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
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import android.util.DisplayMetrics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryController

class SimpleFloatingOverlayService : Service() {
    
    companion object {
        private const val TAG = "SimpleFloatingOverlayService"
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var overlayMenuView: View
    private var isMenuExpanded = false
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SimpleFloatingOverlayService created")
        
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
        // Создаем простую иконку
        floatingView = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_edit)
            setBackgroundColor(ContextCompat.getColor(this@SimpleFloatingOverlayService, android.R.color.holo_blue_dark))
            setPadding(12, 12, 12, 12)
        }
        
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
        Log.d(TAG, "Simple floating icon created and added to window")
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

        Log.d(TAG, "Showing Compose overlay menu")

        // Определяем ориентацию экрана
        val orientation = resources.configuration.orientation
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val overlayWidth = when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> (screenWidth * 0.4).toInt()
            else -> (screenWidth * 0.6).toInt()
        }

        // Создаем ComposeView
        val composeView = ComposeView(this)
        
        val savedStateRegistryOwner = object : SavedStateRegistryOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val controller = SavedStateRegistryController.create(this)
            override val savedStateRegistry: SavedStateRegistry
                get() = controller.savedStateRegistry
            override val lifecycle: Lifecycle
                get() = lifecycleRegistry
        }

        composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

        composeView.setContent {
            OverlayMenu(
                onProfileEditorClick = { performAction1() },
                onButtonEditorClick = { performAction2() },
                onSettingsClick = { performAction3() },
                onCloseOverlayClick = { performAction4() },
                onReturnClick = { performAction5() }
            )
        }

         // Создаем и назначаем фиктивный LifecycleOwner
        val lifecycleOwner = object : LifecycleOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle
                get() = lifecycleRegistry
            init {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }
        }
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)

        overlayMenuView = composeView

        val menuParams = WindowManager.LayoutParams(
            overlayWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 0
        }

        windowManager.addView(overlayMenuView, menuParams)
        isMenuExpanded = true
    }

    private fun closeOverlay() {
        try {
            if (isMenuExpanded) {
                windowManager.removeView(overlayMenuView)
                isMenuExpanded = false
            }
            windowManager.removeView(floatingView)
        } catch (e: Exception) {
            Log.e(TAG, "Error closing overlay", e)
        }
        stopSelf()
    }
    
    private fun hideMenu() {
        if (!isMenuExpanded) return
        
        Log.d(TAG, "Hiding simple overlay menu")
        
        try {
            windowManager.removeView(overlayMenuView)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing menu view", e)
        }
        isMenuExpanded = false
    }
    
    private fun performAction1() {
        Log.d(TAG, "Action 1: Profile Editor")
        if (checkServerPermissions()) {
            performQuickClick()
        }
    }
    
    private fun performAction2() {
        Log.d(TAG, "Action 2: Button Editor")
        if (checkServerPermissions()) {
            performLongClick()
        }
    }
    
    private fun performAction3() {
        Log.d(TAG, "Action 3: Settings")
        if (checkServerPermissions()) {
            openSettings()
        }
    }
    
    private fun performAction4() {
        Log.d(TAG, "Action 4: Close Overlay")
        closeOverlay()
    }
    
    private fun performAction5() {
        Log.d(TAG, "Action 5: Return")
        hideMenu()
        // Здесь можно добавить логику возврата
    }

    private fun performQuickClick() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Performing quick click via server")
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing long click", e)
        }
    }

    private fun openSettings() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Opening settings via server")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening settings", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SimpleFloatingOverlayService destroyed")
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
private fun OverlayMenu(
    onProfileEditorClick: () -> Unit,
    onButtonEditorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseOverlayClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2)) // Синий фон как на изображении
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Редактор профиля
        IconButton(
            icon = android.R.drawable.ic_menu_save, // Иконка сохранения
            text = "Редактор профиля",
            onClick = onProfileEditorClick
        )
        
        // Редактор кнопок
        IconButton(
            icon = android.R.drawable.ic_menu_edit, // Иконка редактирования
            text = "Редактор кнопок",
            onClick = onButtonEditorClick
        )
        
        // Настройки
        IconButton(
            icon = android.R.drawable.ic_menu_manage, // Иконка настроек
            text = "Настройки",
            onClick = onSettingsClick
        )
        
        // Закрыть наложение
        IconButton(
            icon = android.R.drawable.ic_menu_close_clear_cancel, // Иконка закрытия
            text = "Закрыть наложение",
            onClick = onCloseOverlayClick
        )
        
        // Вернуться
        IconButton(
            icon = android.R.drawable.ic_menu_revert, // Иконка возврата
            text = "Вернуться",
            onClick = onReturnClick
        )
    }
}

@Composable
private fun IconButton(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1565C0)) // Темно-синий для иконок
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}