// ui/screens/DeveloperOptionsScreen.kt

package com.example.tapgame.ui.screens

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.services.WifiDebuggingService
import com.example.tapgame.utils.PermissionChecker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    onBack: () -> Unit,
    settingsDataStore: SettingsDataStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isAdbPaired by settingsDataStore.isAdbPairedFlow.collectAsState(initial = false)
    var isPermissionSaved by remember { mutableStateOf(false) } // НОВОЕ СОСТОЯНИЕ
    var isPermissionActive by remember { mutableStateOf<Boolean?>(null) } // ИЗМЕНЕНО НА isPermissionActive
    var isLoading by remember { mutableStateOf(false) }
    var lastCheckTime by remember { mutableStateOf("") }

    fun checkPermissionSaved() {
        scope.launch {
            isPermissionSaved = isAdbPaired
            val message = if (isPermissionSaved) "Разрешение сохранено." else "Разрешение не сохранено."
            snackbarHostState.showSnackbar(message)
        }
    }

    // Функция для проверки активного разрешения
    fun checkPermissionActive() {
        if (isLoading) return
        
        isLoading = true
        scope.launch {
            try {
                val result = PermissionChecker.isPermissionActive(context)
                isPermissionActive = result
                lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date())
                
                if (result) {
                    snackbarHostState.showSnackbar("Активное разрешение: Да (проверено в $lastCheckTime)")
                } else {
                    snackbarHostState.showSnackbar("Активное разрешение: Нет (проверено в $lastCheckTime)")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Ошибка проверки активного разрешения: ${e.message}")
                Log.e("DevOptions", "Check active permission failed", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    val pairingResultReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiDebuggingService.ACTION_PAIRING_RESULT) {
                    scope.launch {
                        // Используем collectAsState значение
                        isPermissionSaved = isAdbPaired
                        isPermissionActive = intent.getBooleanExtra("is_active", false)
                        lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date())
                        
                        val message = when {
                            !isPermissionSaved -> "Сопряжение не удалось"
                            isPermissionActive == true -> "Активное разрешение: Да"
                            else -> "Активное разрешение: Нет (требуется включить Wi-Fi отладку)"
                        }
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }
   
    DisposableEffect(Unit) {
        val filter = IntentFilter(WifiDebuggingService.ACTION_PAIRING_RESULT)
        ContextCompat.registerReceiver(context, pairingResultReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        
        onDispose {
            context.unregisterReceiver(pairingResultReceiver)
        }
    }
    
    LaunchedEffect(Unit) {
    // Подписка на изменения состояния сопряжения
        settingsDataStore.isAdbPairedFlow.collect { isPaired ->
            isPermissionSaved = isPaired
        }
        checkPermissionSaved()
        checkPermissionActive()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Параметры разработчика") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Всегда показываем статус сохраненного разрешения
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (isPermissionSaved) {
                        true -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        false -> Color(0xFFF44336).copy(alpha = 0.2f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isPermissionSaved) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isPermissionSaved) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPermissionSaved) "Разрешение получино (сопряжено)." else "Разрешение не сохранено (не сопряжено)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Карточка активного статуса (только если сопряжено)
            if (isAdbPaired) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (isPermissionActive) {
                            true -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            false -> Color(0xFFF44336).copy(alpha = 0.2f)
                            null -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Проверка активного статуса...")
                        } else {
                            when (isPermissionActive) {
                                true -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Активное разрешение: Да", style = MaterialTheme.typography.titleMedium)
                                    if (lastCheckTime.isNotBlank()) {
                                        Text("Проверено в $lastCheckTime", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                false -> {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFF44336)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Активное разрешение: Нет", style = MaterialTheme.typography.titleMedium)
                                    if (lastCheckTime.isNotBlank()) {
                                        Text("Проверено в $lastCheckTime", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Для активации включите отладку по Wi-Fi в настройках разработчика.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Внимание!!! Если ранее было сопряжение, то повторное подключение не требуется.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                null -> {
                                    Icon(
                                        Icons.Default.QuestionMark,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Активный статус неизвестен", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки управления
                Button(
                    onClick = { checkPermissionActive() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Проверить активный статус")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch {
                            settingsDataStore.setAdbPaired(false)
                            isPermissionSaved = false
                            isPermissionActive = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить сопряжение")
                }
            } else {
                // Экран без сопряжения
                Text(
                    "Для получения постоянного разрешения необходимо выполнить сопряжение.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Button(
                    onClick = {
                        context.startService(WifiDebuggingService.startIntent(context))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Начать сопряжение")
                }
            }
        }
    }
}
