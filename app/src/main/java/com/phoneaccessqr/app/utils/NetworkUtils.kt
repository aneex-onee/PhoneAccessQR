package com.phoneaccessqr.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    /**
     * Get local IP address of the device
     */
    fun getLocalIpAddress(context: Context): String {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                (ipAddress shr 8) and 0xff,
                (ipAddress shr 16) and 0xff,
                (ipAddress shr 24) and 0xff
            )
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    /**
     * Get device MAC address
     */
    fun getMacAddress(): String {
        return try {
            val all = NetworkInterface.getNetworkInterfaces()
            while (all.hasMoreElements()) {
                val nif = all.nextElement()
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * Check if device is connected to WiFi
     */
    fun isConnectedToWifi(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled && wifiManager.connectionInfo != null
    }

    /**
     * Get WiFi SSID (Network name)
     */
    fun getWifiSSID(context: Context): String {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo.ssid.replace("\"", "")
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Check if port is available
     */
    fun isPortAvailable(port: Int): Boolean {
        return try {
            val socket = java.net.ServerSocket(port)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get available port starting from basePort
     */
    fun getAvailablePort(basePort: Int = 9999): Int {
        for (port in basePort..(basePort + 100)) {
            if (isPortAvailable(port)) {
                return port
            }
        }
        return basePort
    }
}
