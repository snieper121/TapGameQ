package com.example.tapgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.conscrypt.Conscrypt // <-- 1. Добавьте этот импорт
import java.security.Security
import com.example.tapgame.ui.MainScreen
import com.example.tapgame.ui.screens.LoadScreen
import com.example.tapgame.ui.theme.TapGameTheme
import com.example.tapgame.data.SettingsDataStore
import com.example.tapgame.viewmodel.AppListViewModel

class MainActivity : ComponentActivity() {
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        settingsDataStore = SettingsDataStore(applicationContext)
        val viewModel: AppListViewModel by viewModels()
    
        setContent {
            val darkTheme by settingsDataStore.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)
            val isLoading by viewModel.isLoading // Исправлено: прямое использование State
            val minDelayPassed = remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                Log.d("MainActivity", "LaunchedEffect: Starting minDelay")
                delay(1000)
                minDelayPassed.value = true
                Log.d("MainActivity", "LaunchedEffect: minDelayPassed = true")
            }

            LaunchedEffect(isLoading) {
                Log.d("MainActivity", "isLoading: $isLoading")
            }
            
            
            
            TapGameTheme(darkTheme = darkTheme) {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                BackHandler {
                    scope.launch {
                        if (snackbarHostState.currentSnackbarData == null) {
                            snackbarHostState.showSnackbar("Нажмите еще раз для выхода")
                        } else {
                            finish()
                        }
                    }
                }

                Log.d("MainActivity", "Evaluating screen: isLoading=$isLoading, minDelayPassed=${minDelayPassed.value}")
                if (isLoading || !minDelayPassed.value) {
                    Log.d("MainActivity", "Rendering LoadScreen")
                    LoadScreen()
                } else {
                    Log.d("MainActivity", "Rendering MainScreen")
                    MainScreen(
                        settingsDataStore = settingsDataStore,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}