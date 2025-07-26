// Файл: services/WifiDebuggingService.kt
// ФИНАЛЬНАЯ ВЕРСИЯ С BROADCAST И НАДЕЖНЫМ ПОИСКОМ

package com.example.tapgame.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.lifecycle.Observer
import com.example.tapgame.R
import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.utils.PermissionChecker
import com.example.tapgame.utils.NetworkUtils
import kotlinx.coroutines.*
import moe.shizuku.manager.adb.*
import java.net.ConnectException

@SuppressLint("NewApi")
class WifiDebuggingService : Service() {

    companion object {
        const val ACTION_PAIRING_RESULT = "com.example.tapgame.PAIRING_RESULT"
        
        private const val CHANNEL_ID = "wifi_debug_channel"
        private const val NOTIFICATION_ID = 101
        private const val ACTION_START_PAIRING = "com.example.tapgame.START_PAIRING"
        private const val ACTION_STOP = "com.example.tapgame.STOP"
        private const val ACTION_REPLY_PAIRING_CODE = "com.example.tapgame.REPLY_PAIRING_CODE"
        private const val ACTION_RETRY_PAIRING = "com.example.tapgame.RETRY_PAIRING"
        private const val KEY_REMOTE_INPUT = "pairing_code"
        private const val KEY_PORT = "port"

        fun startIntent(context: Context): Intent {
            return Intent(context, WifiDebuggingService::class.java).setAction(ACTION_START_PAIRING)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var settingsDataStore: SettingsDataStore
    private var pairingMdns: AdbMdns? = null

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PAIRING -> {
                val notification = createSearchingNotification()
                startForeground(notification)
                startPairingMdnsSearch()
            }
            ACTION_REPLY_PAIRING_CODE -> {
                val code = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_REMOTE_INPUT)?.toString() ?: ""
                val port = intent.getIntExtra(KEY_PORT, -1)
                if (port != -1 && code.isNotBlank()) {
                    performPairingAndStartServer(code, port)
                }
            }
            ACTION_STOP -> stopService()
            ACTION_RETRY_PAIRING -> {
                val notification = createSearchingNotification()
                startForeground(notification)
                startPairingMdnsSearch()
            }
        }
        return START_STICKY
    }

    private fun startPairingMdnsSearch() {
        pairingMdns?.stop()
        pairingMdns = AdbMdns(this, AdbMdns.TLS_PAIRING, Observer { port ->
            if (port > 0) {
                updateNotification(createInputNotification(port))
                pairingMdns?.stop()
            }
        }).apply { start() }
    }

    // services/WifiDebuggingService.kt
    private fun performPairingAndStartServer(code: String, port: Int) {
        updateNotification(createWorkingNotification("Выполняется сопряжение..."))
        serviceScope.launch {
            var success = false
            var error: Throwable? = null
            try {
                val keyStore = PreferenceAdbKeyStore(getSharedPreferences("adb_key", Context.MODE_PRIVATE))
                val key = AdbKey(keyStore, "TapGameKey")
    
                // Получаем локальный IP-адрес
                val localIp = NetworkUtils.getLocalIpAddress(applicationContext) ?: "127.0.0.1"
                Log.d("WifiDebuggingService", "Используем IP-адрес: $localIp")
    
                Log.d("WifiDebuggingService", "Шаг 1: Выполняем сопряжение...")
                val pairingClient = AdbPairingClient(localIp, port, code, key)
                pairingClient.start()
                pairingClient.close()
                Log.d("WifiDebuggingService", "Сопряжение успешно.")
    
                updateNotification(createWorkingNotification("Запуск службы..."))
                Log.d("WifiDebuggingService", "Шаг 2: Ищем порт для подключения...")
                val connectPort = findAdbConnectPort()
                if (connectPort == -1) throw Exception("Не удалось найти порт для подключения.")
                settingsDataStore.setAdbConnectPort(connectPort)
                Log.d("WifiDebuggingService", "Порт для подключения найден: $connectPort")
    
                Log.d("WifiDebuggingService", "Шаг 3: Подключаемся и запускаем сервер-маркер...")
                val adbClient = AdbClient(localIp, connectPort, key)
                adbClient.connect()
    
                adbClient.shell("killall -9 sh")
                adbClient.shell("exec -a tapgame_marker sleep 9999999")
                adbClient.close()
                Log.d("WifiDebuggingService", "Сервер-маркер успешно запущен.")
                
                success = true
            } catch (e: Throwable) {
                Log.e("WifiDebuggingService", "Ошибка на этапе сопряжения или запуска", e)
                error = e
                success = false
            } finally {
                handleResult(success, error)
            }
        }
    }

    private suspend fun findAdbConnectPort(): Int {
        for (attempt in 1..3) {
            Log.d("WifiDebuggingService", "Попытка найти порт подключения №$attempt...")
            val portDeferred = CompletableDeferred<Int>()
            val adbConnectMdns = AdbMdns(applicationContext, AdbMdns.TLS_CONNECT, Observer { port ->
                if (port > 0 && !portDeferred.isCompleted) {
                    portDeferred.complete(port)
                }
            })
            adbConnectMdns.start()
            val resultPort = withTimeoutOrNull(5000) { portDeferred.await() }
            adbConnectMdns.stop()
            if (resultPort != null) return resultPort
            Log.w("WifiDebuggingService", "Попытка №$attempt не удалась, ждем 1 секунду...")
            delay(1000)
        }
        return -1
    }
    private fun handleResult(success: Boolean, error: Throwable?) {
        serviceScope.launch {
            delay(2000) // Увеличиваем задержку перед проверкой
            settingsDataStore.setAdbPaired(success)
            
            var isPermissionActive = false // Выносим объявление переменной вне блока if
            if (success) {
                // Добавляем несколько попыток проверки
                repeat(3) { attempt ->
                    isPermissionActive = PermissionChecker.isPermissionActive(applicationContext)
                    if (isPermissionActive) return@repeat
                    delay(1000L * (attempt + 1))
                }
                Log.d("WifiDebuggingService", "Permission check result: $isPermissionActive")
            }
            
            sendBroadcast(Intent(ACTION_PAIRING_RESULT).apply {
                putExtra("is_active", isPermissionActive)
            }) // Добавляем закрывающую скобку для apply
        }
    
        val title = if (success) "Разрешение получено!" else "Ошибка"
        val text = if (success) "Теперь можно выключить отладку по Wi-Fi." else error?.message ?: "Неизвестная ошибка."
        updateNotification(createResultNotification(title, text, !success))
    }

    private fun stopService() {
        pairingMdns?.stop()
        stopForeground(true)
        stopSelf()
    }

    // --- Функции для уведомлений (без изменений) ---
    private fun createSearchingNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_debug)
            .setContentTitle("Поиск устройства для сопряжения...")
            .setContentText("Пожалуйста, откройте меню 'Подключение устройства с помощью кода' в настройках.")
            .setOngoing(true)
            .addAction(createStopAction())
            .build()
    }

    private fun createInputNotification(port: Int): Notification {
        val remoteInput = RemoteInput.Builder(KEY_REMOTE_INPUT).setLabel("Код сопряжения").build()
        val replyIntent = Intent(this, WifiDebuggingService::class.java)
            .setAction(ACTION_REPLY_PAIRING_CODE)
            .putExtra(KEY_PORT, port)
        val replyPendingIntent = PendingIntent.getService(this, 2, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val action = NotificationCompat.Action.Builder(R.drawable.ic_pair, "Ввести код", replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_debug)
            .setContentTitle("Устройство найдено!")
            .setContentText("Введите 6-значный код из настроек.")
            .setOngoing(true)
            .addAction(action)
            .addAction(createStopAction())
            .build()
    }

    private fun createWorkingNotification(title: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_debug)
            .setContentTitle(title)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()
    }

    private fun createResultNotification(title: String, text: String?, addRetry: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_debug)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .addAction(createStopAction())

        if (addRetry) {
            val retryIntent = Intent(this, WifiDebuggingService::class.java).setAction(ACTION_RETRY_PAIRING)
            val retryPendingIntent = PendingIntent.getService(this, 4, retryIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(R.drawable.ic_debug, "Повторить", retryPendingIntent)
        }
        return builder.build()
    }
    
    private fun createStopAction(): NotificationCompat.Action {
        val stopIntent = Intent(this, WifiDebuggingService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(R.drawable.ic_close, "Стоп", stopPendingIntent).build()
    }

    private fun updateNotification(notification: Notification) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun startForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pairingMdns?.stop()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Сопряжение ADB", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
