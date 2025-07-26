// utils/PermissionChecker.kt

package com.example.tapgame.utils

import android.content.Context
import android.util.Log
import com.example.tapgame.data.SettingsDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import java.net.ConnectException

object PermissionChecker {

    // Изменяем логику: теперь эта функция проверяет, было ли сопряжение успешно.
    // Она не проверяет активное ADB-соединение, так как оно отключается при выключении Wi-Fi отладки.
    suspend fun isPermissionSaved(context: Context): Boolean = withContext(Dispatchers.IO) {
        val settingsDataStore = SettingsDataStore(context)
        val isAdbPaired = settingsDataStore.isAdbPairedFlow.first()

        if (isAdbPaired) {
            Log.d("PermissionChecker", "ADB paired status: true. Permission is considered saved.")
            return@withContext true
        } else {
            Log.d("PermissionChecker", "ADB paired status: false. Permission is not saved.")
            return@withContext false
        }
    }
    suspend fun isPermissionActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        val settingsDataStore = SettingsDataStore(context)
        val isAdbPaired = settingsDataStore.isAdbPairedFlow.first()
    
        if (!isAdbPaired) {
            Log.d("PermissionChecker", "ADB not paired, active permission check skipped.")
            return@withContext false
        }
    
        val adbConnectPort = settingsDataStore.adbConnectPortFlow.first()
        if (adbConnectPort == -1) {
            Log.d("PermissionChecker", "ADB connect port not found, active permission check skipped.")
            return@withContext false
        }
    
        try {
            val keyStore = PreferenceAdbKeyStore(context.getSharedPreferences("adb_key", Context.MODE_PRIVATE))
            val key = AdbKey(keyStore, "TapGameKey")
    
            // Добавляем задержку перед проверкой
            delay(1000)
    
            val localIp = NetworkUtils.getLocalIpAddress(context) ?: run {
                Log.w("PermissionChecker", "Failed to get local IP, using fallback")
                "127.0.0.1"
            }
            
            Log.d("PermissionChecker", "Checking active permission with IP: $localIp, port: $adbConnectPort")
    
            val adbClient = AdbClient(localIp, adbConnectPort, key)
            adbClient.connect()
    
            val result = adbClient.shell("echo hello")
            adbClient.close()
    
            val isActive = result.trim() == "hello"
            Log.d("PermissionChecker", "ADB connection test result: $isActive, shell output: \"$result\"")
            isActive
        } catch (e: ConnectException) {
            Log.w("PermissionChecker", "ConnectException: ADB server not reachable or connection refused. Active permission not found.")
            false
        } catch (e: Exception) {
            Log.e("PermissionChecker", "Error checking active permission via ADB client", e)
            false
        }
    }
}
