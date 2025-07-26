package com.example.tapgame.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tapgame.services.WifiDebuggingService

class WifiDebugStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.settings.ADB_WIFI_ENABLED_CHANGED") {
            Log.d("WifiDebugStateReceiver", "Wifi debug state changed. Starting service to update notification.")
            // Просто запускаем сервис. Он сам разберется, что делать.
            context.startService(WifiDebuggingService.startIntent(context))
        }
    }
}
