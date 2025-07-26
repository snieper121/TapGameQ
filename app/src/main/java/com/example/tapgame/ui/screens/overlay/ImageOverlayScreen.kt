package com.example.tapgame.ui.screens.overlay

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImageOverlayScreen(onDismiss: () -> Unit) {
    var imageUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context.findActivity()

    // Включение иммерсивного режима и игнорирование выреза
    DisposableEffect(Unit) {
        activity?.let {
            // Отключение отступов под системные элементы
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            // Игнорирование выреза экрана
            it.window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            val windowInsetsController = WindowInsetsControllerCompat(it.window, it.window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        }
        onDispose {
            activity?.let {
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                it.window.attributes.layoutInDisplayCutoutMode = 
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                val windowInsetsController = WindowInsetsControllerCompat(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    // Отслеживание появления системной строки и повторное скрытие через 2 секунды
    LaunchedEffect(Unit) {
        while (true) {
            if (view.isAttachedToWindow) {
                val insets = view.rootWindowInsets
                if (insets?.isVisible(WindowInsetsCompat.Type.statusBars()) == true) {
                    delay(2000) // Задержка 2 секунды
                    activity?.let { act ->
                        val windowInsetsController = WindowInsetsControllerCompat(act.window, act.window.decorView)
                        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
            delay(500) // Проверка каждые 0.5 секунды
        }
    }

    // Лаунчер для выбора изображения
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri?.toString()
        if (uri == null) onDismiss() // Закрыть, если не выбрано изображение
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Выбрать изображение")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

// Вспомогательная функция для получения Activity
fun Context.findActivity(): Activity? {
    var currentContext: Context = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}