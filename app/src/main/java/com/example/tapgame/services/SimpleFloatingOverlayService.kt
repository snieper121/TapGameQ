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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

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
        
        Log.d(TAG, "Showing simple overlay menu")
        
        overlayMenuView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(this@SimpleFloatingOverlayService, android.R.color.black))
            setPadding(16, 16, 16, 16)
            
            // Кнопка 1
            addView(createMenuButton("Клик") { performAction1() })
            addView(createMenuButton("Долгий") { performAction2() })
            addView(createMenuButton("Свайп") { performAction3() })
            addView(createMenuButton("Настройки") { performAction4() })
            addView(createMenuButton("Закрыть") { performAction5() })
        }
        
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
    
    private fun createMenuButton(text: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(this@SimpleFloatingOverlayService, android.R.color.white))
            setPadding(8, 4, 8, 4)
            setOnClickListener { onClick() }
        }
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
        Log.d(TAG, "Action 1: Quick Click")
        if (checkServerPermissions()) {
            performQuickClick()
        }
    }
    
    private fun performAction2() {
        Log.d(TAG, "Action 2: Long Click")
        if (checkServerPermissions()) {
            performLongClick()
        }
    }
    
    private fun performAction3() {
        Log.d(TAG, "Action 3: Swipe")
        if (checkServerPermissions()) {
            performSwipe()
        }
    }
    
    private fun performAction4() {
        Log.d(TAG, "Action 4: Settings")
        if (checkServerPermissions()) {
            openSettings()
        }
    }
    
    private fun performAction5() {
        Log.d(TAG, "Action 5: Close Menu")
        hideMenu()
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

    private fun performSwipe() {
        try {
            val server = com.example.tapgame.server.MyPersistentServer.getInstance()
            if (server?.isPermissionActive() == true) {
                Log.d(TAG, "Performing swipe via server")
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