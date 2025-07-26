// Файл: data/SettingsDataStore.kt
// СКОПИРУЙТЕ И ЗАМЕНИТЕ ВСЕ СОДЕРЖИМОЕ

package com.example.tapgame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        val OVERLAY_KEY = booleanPreferencesKey("overlay_enabled")
        val BACKGROUND_RUN_KEY = booleanPreferencesKey("background_run_enabled")
        val DEVELOPER_OPTIONS_KEY = booleanPreferencesKey("developer_options_enabled")
        val USB_DEBUGGING_KEY = booleanPreferencesKey("usb_debugging_enabled")
        val WIFI_DEBUGGING_KEY = booleanPreferencesKey("wifi_debugging_enabled")
        val AUTO_PORT_KEY = booleanPreferencesKey("auto_port_enabled")
        val ACTIVATE_KEY = booleanPreferencesKey("activate_enabled")
        val WIFI_IP_ADDRESS = stringPreferencesKey("wifi_ip_address")
        val WIFI_PORT = stringPreferencesKey("wifi_port")
        val WIFI_PAIRING_CODE = stringPreferencesKey("wifi_pairing_code")
        val LAST_KNOWN_IP = stringPreferencesKey("last_known_ip")
        // Добавить в companion object:
        val ADB_CONNECT_PORT = intPreferencesKey("adb_connect_port")
        // ДОБАВЛЕН КЛЮЧ
        val ADB_PAIRED_KEY = booleanPreferencesKey("adb_paired")
    }
    
    // Добавить в класс:
    val adbConnectPortFlow: Flow<Int> = dataStore.data
        .map { preferences -> preferences[ADB_CONNECT_PORT] ?: -1 }
    
    suspend fun setAdbConnectPort(port: Int) {
        dataStore.edit { settings ->
            settings[ADB_CONNECT_PORT] = port
        }
    }
    
    // --- НОВЫЙ КОД ДЛЯ СТАТУСА СОПРЯЖЕНИЯ ---
    val isAdbPairedFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[ADB_PAIRED_KEY] ?: false }

    suspend fun setAdbPaired(isPaired: Boolean) {
        dataStore.edit { settings ->
            settings[ADB_PAIRED_KEY] = isPaired
        }
    }
    
    // --- ВАШ СТАРЫЙ КОД, ИСПРАВЛЕННЫЙ ДЛЯ ЕДИНООБРАЗИЯ ---
    val darkModeFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: false }

    suspend fun saveDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
    
    val overlayFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[OVERLAY_KEY] ?: false }

    suspend fun saveOverlaySetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OVERLAY_KEY] = enabled
        }
    }

    val backgroundRunFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[BACKGROUND_RUN_KEY] ?: false }

    suspend fun saveBackgroundRunSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_RUN_KEY] = enabled
        }
    }

    val developerOptionsFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[DEVELOPER_OPTIONS_KEY] ?: false }

    suspend fun saveDeveloperOptionsSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEVELOPER_OPTIONS_KEY] = enabled
        }
    }

    val usbDebuggingFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[USB_DEBUGGING_KEY] ?: false }

    suspend fun saveUsbDebuggingSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USB_DEBUGGING_KEY] = enabled
        }
    }

    val wifiDebuggingFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[WIFI_DEBUGGING_KEY] ?: false }

    suspend fun saveWifiDebuggingSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WIFI_DEBUGGING_KEY] = enabled
        }
    }

    val autoPortFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[AUTO_PORT_KEY] ?: false }

    suspend fun saveAutoPortSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PORT_KEY] = enabled
        }
    }

    val activateFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[ACTIVATE_KEY] ?: false }

    suspend fun saveActivateSetting(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ACTIVATE_KEY] = enabled
        }
    }

    suspend fun saveWifiPairingData(ipAddress: String, port: String, pairingCode: String) {
        dataStore.edit { preferences ->
            preferences[WIFI_IP_ADDRESS] = ipAddress
            preferences[WIFI_PORT] = port
            preferences[WIFI_PAIRING_CODE] = pairingCode
        }
    }

    val wifiPairingDataFlow: Flow<Triple<String, String, String>> = dataStore.data.map { preferences ->
        Triple(
            preferences[WIFI_IP_ADDRESS] ?: "",
            preferences[WIFI_PORT] ?: "",
            preferences[WIFI_PAIRING_CODE] ?: ""
        )
    }
    suspend fun saveLastKnownIp(ip: String) {
        dataStore.edit { preferences ->
            preferences[LAST_KNOWN_IP] = ip
        }
    }

    val lastKnownIpFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[LAST_KNOWN_IP] ?: "" }
}
