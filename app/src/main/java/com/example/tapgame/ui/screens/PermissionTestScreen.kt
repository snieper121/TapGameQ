package com.example.tapgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tapgame.ui.screens.overlay.OverlayManager
import com.example.tapgame.server.MyPersistentServer

@Composable
fun PermissionTestScreen() {
    var testResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🧪 Тест разрешений",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = {
                isLoading = true
                testResult = runPermissionTests()
                isLoading = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Запустить тест разрешений")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (OverlayManager.hasOverlayPermission(context)) {
                    OverlayManager.startOverlay(context)
                } else {
                    OverlayManager.requestOverlayPermission(context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧪 Тест оверлея")
        }
        
        if (testResult.isNotEmpty()) {
            Text(
                text = testResult,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

private fun runPermissionTests(): String {
    return try {
        val server = MyPersistentServer.getInstance()
        val result = StringBuilder()
        
        result.append("📊 Статус разрешений TapGame:\n\n")
        
        result.append("🖥️ Сервер запущен: ${if (server != null) "✅" else "❌"}\n")
        
        if (server != null) {
            result.append("🔐 Разрешения в конфиге: ${if (server.isPermissionActive()) "✅" else "❌"}\n")
            result.append("🎯 Оверлей: ${if (server.canShowOverlay()) "✅" else "❌"}\n")
            result.append("🖱️ Инъекция ввода: ${if (server.canInjectInput()) "✅" else "❌"}\n")
            result.append("📸 Захват экрана: ${if (server.canCaptureScreen()) "✅" else "❌"}\n")
            result.append("🪟 Управление окнами: ${if (server.canControlWindows()) "✅" else "❌"}\n")
        }
        
        result.toString()
    } catch (e: Exception) {
        "❌ Ошибка проверки: ${e.message}"
    }
}