// utils/NetworkUtils.kt
package com.example.tapgame.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(context: Context? = null): String? {
        return try {
            // Сначала пробуем получить IP через NetworkInterface (более надежно)
            NetworkInterface.getNetworkInterfaces()?.toList()?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
                ?: getActiveNetworkIp(context) // Fallback к системному способу
        } catch (e: Exception) {
            null
        }
    }

    private fun getActiveNetworkIp(context: Context?): String? {
        if (context == null) return null
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return null
            val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
            
            linkProperties.linkAddresses.firstOrNull {
                it.address is Inet4Address
            }?.address?.hostAddress
        } catch (e: Exception) {
            null
        }
    }
}