package com.example.tapgame.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.utils.FeedbackManager
import com.example.tapgame.ui.theme.TapGameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsDataStore: SettingsDataStore, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val feedbackManager = remember { FeedbackManager(context, settingsDataStore) }

    val darkMode by settingsDataStore.darkModeFlow.collectAsState(initial = false)
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Настройки  ")
                        }
                    },
                    navigationIcon = {}, // Добавляем пустую иконку навигации
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ListItem(
                        headlineContent = { Text("Тёмная тема") },
                        trailingContent = {
                            Switch(
                                checked = darkMode,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        settingsDataStore.saveDarkMode(it)
                                        //feedbackManager.performFeedback()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ListItem(
                        headlineContent = { Text("Включить управление с сенсором") },
                        trailingContent = {
                            val isChecked = remember { mutableStateOf(false) }
                            Switch(
                                checked = isChecked.value,
                                onCheckedChange = { newValue ->
                                    isChecked.value = newValue // Обновляем состояние для анимации
                                    // TODO: Добавить действие для переключателя здесь, если потребуется
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                feedbackManager.performFeedback()
                                navController.navigate("permissions")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Разрешения приложения")
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                feedbackManager.performFeedback()
                                navController.navigate("imageOverlay")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Настройки наложения кнопок")
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                feedbackManager.performFeedback()
                                navController.navigate("developer_options")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Параметры разработчика")
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                feedbackManager.performFeedback()
                                navController.navigate("info")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Информация о приложении")
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                feedbackManager.performFeedback()
                                // TODO: Добавить действие для кнопки (например, сброс настроек)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Сбросить настройки")
                    }
                }
            }
        }
    )
}