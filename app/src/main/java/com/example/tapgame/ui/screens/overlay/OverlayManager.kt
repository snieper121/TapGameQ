package com.example.tapgame.ui.screens.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.example.tapgame.services.SimpleFloatingOverlayService

object OverlayManager {
    
    private const val TAG = "OverlayManager"
    
    fun startOverlay(context: Context) {
        Log.d(TAG, "Starting overlay")
        val intent = Intent(context, SimpleFloatingOverlayService::class.java)
        context.startService(intent)
    }
    
    fun stopOverlay(context: Context) {
        Log.d(TAG, "Stopping overlay")
        val intent = Intent(context, SimpleFloatingOverlayService::class.java)
        context.stopService(intent)
    }
    
    fun isOverlayRunning(context: Context): Boolean {
        // Упрощенная проверка - можно улучшить
        return false
    }
    
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun requestOverlayPermission(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}