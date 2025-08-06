package com.example.tapgame.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

class FloatingOverlayService : Service() {
    
    companion object {
        private const val TAG = "FloatingOverlayService"
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var overlayMenuView: View
    private var isMenuExpanded = false
    
    // Позиция оверлея
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FloatingOverlayService created")
        
        // Проверяем разрешения через сервер
        if (checkServerPermissions()) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            createFloatingIcon()
        } else {
            Log.e(TAG, "Server permissions not available, stopping service")
            stopSelf()
        }
    }

    private fun checkServerPermissions(): Boolean {
        return try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            server?.isPermissionActive() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking server permissions", e)
            false
        }
    }
    
    private fun createFloatingIcon() {
        // Создаем плавающую иконку
        floatingView = ComposeView(this).apply {
            setContent {
                FloatingIcon(
                    onIconClick = { toggleMenu() },
                    onIconLongPress = { /* Дополнительные действия */ }
                )
            }
        }
        
        // Параметры окна для плавающей иконки
        val floatingParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }
        
        // Добавляем обработчик касаний для перемещения
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = floatingParams.x
                    initialY = floatingParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    floatingParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    floatingParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, floatingParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(event.rawX - initialTouchX) < 10 && 
                        Math.abs(event.rawY - initialTouchY) < 10) {
                        toggleMenu()
                    }
                    true
                }
                else -> false
            }
        }
        
        windowManager.addView(floatingView, floatingParams)
        Log.d(TAG, "Floating icon created and added to window")
    }
    
    private fun toggleMenu() {
        if (isMenuExpanded) {
            hideMenu()
        } else {
            showMenu()
        }
    }
    
    private fun showMenu() {
        if (isMenuExpanded) return
        
        Log.d(TAG, "Showing overlay menu")
        
        // Создаем меню оверлея
        overlayMenuView = ComposeView(this).apply {
            setContent {
                OverlayMenu(
                    onButton1Click = { performAction1() },
                    onButton2Click = { performAction2() },
                    onButton3Click = { performAction3() },
                    onButton4Click = { performAction4() },
                    onButton5Click = { performAction5() }
                )
            }
        }
        
        // Параметры окна для меню
        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 100
        }
        
        windowManager.addView(overlayMenuView, menuParams)
        isMenuExpanded = true
    }
    
    private fun hideMenu() {
        if (!isMenuExpanded) return
        
        Log.d(TAG, "Hiding overlay menu")
        
        try {
            windowManager.removeView(overlayMenuView)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing menu view", e)
        }
        isMenuExpanded = false
    }
    
    // Действия кнопок меню
    private fun performAction1() {
        Log.d(TAG, "Action 1: Quick Click")
        if (checkServerPermissions()) {
            // Здесь будет логика быстрого клика через сервер
            performQuickClick()
        }
    }
    
    private fun performAction2() {
        Log.d(TAG, "Action 2: Long Click")
        if (checkServerPermissions()) {
            // Здесь будет логика долгого клика через сервер
            performLongClick()
        }
    }
    
    private fun performAction3() {
        Log.d(TAG, "Action 3: Swipe")
        if (checkServerPermissions()) {
            // Здесь будет логика свайпа через сервер
            performSwipe()
        }
    }
    
    private fun performAction4() {
        Log.d(TAG, "Action 4: Settings")
        if (checkServerPermissions()) {
            // Здесь будет открытие настроек через сервер
            openSettings()
        }
    }
    
    private fun performAction5() {
        Log.d(TAG, "Action 5: Close Menu")
        hideMenu()
    }

    // Реализация действий через сервер
    private fun performQuickClick() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Performing quick click via server")
                // Здесь будет вызов метода сервера для симуляции клика
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing quick click", e)
        }
    }

    private fun performLongClick() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Performing long click via server")
                // Здесь будет вызов метода сервера для симуляции долгого клика
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing long click", e)
        }
    }

    private fun performSwipe() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Performing swipe via server")
                // Здесь будет вызов метода сервера для симуляции свайпа
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe", e)
        }
    }

    private fun openSettings() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Opening settings via server")
                // Здесь будет вызов метода сервера для открытия настроек
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening settings", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FloatingOverlayService destroyed")
        try {
            windowManager.removeView(floatingView)
            if (isMenuExpanded) {
                windowManager.removeView(overlayMenuView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing views", e)
        }
    }
}

@Composable
fun FloatingIcon(
    onIconClick: () -> Unit,
    onIconLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF2196F3))
            .clickable { onIconClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Gamepad,
            contentDescription = "GG Mouse Pro",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun OverlayMenu(
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
    onButton3Click: () -> Unit,
    onButton4Click: () -> Unit,
    onButton5Click: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xCC000000),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка 1: Быстрый клик
        OverlayButton(
            icon = Icons.Default.TouchApp,
            label = "Клик",
            onClick = onButton1Click
        )
        
        // Кнопка 2: Долгий клик
        OverlayButton(
            icon = Icons.Default.Timer,
            label = "Долгий",
            onClick = onButton2Click
        )
        
        // Кнопка 3: Свайп
        OverlayButton(
            icon = Icons.Default.Swipe,
            label = "Свайп",
            onClick = onButton3Click
        )
        
        // Кнопка 4: Настройки
        OverlayButton(
            icon = Icons.Default.Settings,
            label = "Настройки",
            onClick = onButton4Click
        )
        
        // Кнопка 5: Закрыть
        OverlayButton(
            icon = Icons.Default.Close,
            label = "Zакрыть",
            onClick = onButton5Click
        )
    }
}

@Composable
fun OverlayButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}