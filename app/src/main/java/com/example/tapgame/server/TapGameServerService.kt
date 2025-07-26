package com.example.tapgame.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import rikka.shizuku.server.ShizukuService

class TapGameServerService : Service() {
    
    companion object {
        private const val TAG = "TapGameServerService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TapGameServerService created")
        
        // Инициализация Shizuku сервера
        try {
            // Запуск Shizuku сервера в отдельном потоке
            Thread {
                ShizukuService.main(arrayOf())
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ShizukuService", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind called")
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TapGameServerService destroyed")
    }
}

