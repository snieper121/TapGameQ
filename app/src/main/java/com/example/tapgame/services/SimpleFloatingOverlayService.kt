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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
        val overlayWidth = (screenWidth * 0.5).toInt() // 50% ширины экрана

        // Создаем ComposeView
        val composeView = ComposeView(this)
        
        val savedStateRegistryOwner = object : SavedStateRegistryOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val controller = SavedStateRegistryController.create(this)
            override val savedStateRegistry: SavedStateRegistry
                get() = controller.savedStateRegistry
            override val lifecycle: Lifecycle
                get() = lifecycleRegistry

            init {
                lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
                controller.performAttach()
                controller.performRestore(null)
                lifecycleRegistry.currentState = Lifecycle.State.CREATED
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }
        }

        composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)

        composeView.setContent {
            val density = LocalDensity.current
            OverlayMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(with(density) { overlayWidth.toDp() }),
                iconSize = 32.dp, // уменьшенный размер иконок
                iconSpacing = 4.dp, // уменьшенное расстояние между иконками
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
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    iconSpacing: Dp = 4.dp,
    onProfileEditorClick: () -> Unit,
    onButtonEditorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseOverlayClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1976D2))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            icon = android.R.drawable.ic_menu_save,
            text = "Редактор профиля",
            onClick = onProfileEditorClick,
            iconSize = iconSize
        )
        IconButton(
            icon = android.R.drawable.ic_menu_edit,
            text = "Редактор кнопок",
            onClick = onButtonEditorClick,
            iconSize = iconSize
        )
        IconButton(
            icon = android.R.drawable.ic_menu_manage,
            text = "Настройки",
            onClick = onSettingsClick,
            iconSize = iconSize
        )
        IconButton(
            icon = android.R.drawable.ic_menu_close_clear_cancel,
            text = "Закрыть наложение",
            onClick = onCloseOverlayClick,
            iconSize = iconSize
        )
        IconButton(
            icon = android.R.drawable.ic_menu_revert,
            text = "Вернуться",
            onClick = onReturnClick,
            iconSize = iconSize
        )
    }
}
@Composable
private fun IconButton(
    icon: Int,
    text: String,
    onClick: () -> Unit,
    iconSize: Dp = 32.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(iconSize + 16.dp) // кнопка чуть больше иконки
                .clip(CircleShape)
                .background(Color(0xFF1565C0))
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}