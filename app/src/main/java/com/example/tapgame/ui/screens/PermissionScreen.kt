package com.example.tapgame.ui.screens

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.ui.theme.AddButtonColor
import com.example.tapgame.ui.theme.RemoveButtonColor
import kotlinx.coroutines.launch

@Composable
fun PermissionScreen(onDismiss: () -> Unit, settingsDataStore: SettingsDataStore) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val overlayPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // После возврата из настроек проверяем разрешение и обновляем состояние
        val isOverlayEnabled = Settings.canDrawOverlays(context)
        coroutineScope.launch {
            settingsDataStore.saveOverlaySetting(isOverlayEnabled)
        }
    }
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // После возврата из настроек проверяем разрешение и обновляем состояние
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBackgroundRunEnabled = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        coroutineScope.launch {
            settingsDataStore.saveBackgroundRunSetting(isBackgroundRunEnabled)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Настройки разрешений",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Рекомендуемые настройки
        Text(
            text = "Рекомендуемые настройки",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            var isPowerSavingEnabled by remember { mutableStateOf(false) }
            ListItem(
                headlineContent = { Text("Режим энергосбережения") },
                trailingContent = {
                    Switch(
                        checked = isPowerSavingEnabled,
                        onCheckedChange = { isPowerSavingEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RemoveButtonColor,
                            checkedTrackColor = RemoveButtonColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = AddButtonColor,
                            uncheckedTrackColor = AddButtonColor.copy(alpha = 0.5f)
                        )
                    )
                }
            )
        }

        // Обязательные настройки
        Text(
            text = "Обязательные настройки",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            val isOverlayEnabled by settingsDataStore.overlayFlow.collectAsState(initial = Settings.canDrawOverlays(context))
            ListItem(
                headlineContent = { Text("Наложение окна") },
                trailingContent = {
                    Switch(
                        checked = isOverlayEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !Settings.canDrawOverlays(context)) {
                                // Открываем системные настройки для наложения окна
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                overlayPermissionLauncher.launch(intent)
                            } else {
                                coroutineScope.launch {
                                    settingsDataStore.saveOverlaySetting(enabled)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AddButtonColor,
                            checkedTrackColor = AddButtonColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = RemoveButtonColor,
                            uncheckedTrackColor = RemoveButtonColor.copy(alpha = 0.5f)
                        )
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isBackgroundRunEnabled by settingsDataStore.backgroundRunFlow.collectAsState(
                initial = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            )
            ListItem(
                headlineContent = { Text("Запуск в фоновом режиме") },
                trailingContent = {
                    Switch(
                        checked = isBackgroundRunEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                                // Открываем системные настройки для игнорирования оптимизации батареи
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                                batteryOptimizationLauncher.launch(intent)
                            } else {
                                coroutineScope.launch {
                                    settingsDataStore.saveBackgroundRunSetting(enabled)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AddButtonColor,
                            checkedTrackColor = AddButtonColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = RemoveButtonColor,
                            uncheckedTrackColor = RemoveButtonColor.copy(alpha = 0.5f)
                        )
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Закрыть")
        }
    }
}