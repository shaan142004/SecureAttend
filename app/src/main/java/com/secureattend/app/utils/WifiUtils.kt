package com.secureattend.app.utils

import android.content.Context
import android.net.wifi.WifiManager

/**
 * Reads the BSSID (unique hardware MAC address) of the Wi-Fi access point
 * the phone is currently connected to, and compares it against the
 * classroom's registered router BSSID.
 *
 * IMPORTANT: On Android 8.1+ this requires ACCESS_FINE_LOCATION permission
 * to be granted at runtime, or the BSSID will show up as "02:00:00:00:00:00".
 */
object WifiUtils {

    /** Returns the BSSID of the currently connected Wi-Fi network, or null if not connected. */
    fun getCurrentBssid(context: Context): String? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        val connectionInfo = wifiManager.connectionInfo ?: return null
        val bssid = connectionInfo.bssid

        return if (bssid.isNullOrEmpty() || bssid == "02:00:00:00:00:00") {
            null // Not connected, or location permission not granted yet
        } else {
            bssid.lowercase()
        }
    }

    /** Checks whether the phone is connected to the given classroom router. */
    fun isConnectedToClassroomWifi(context: Context, classroomBssid: String): Boolean {
        val currentBssid = getCurrentBssid(context) ?: return false
        return currentBssid.equals(classroomBssid.lowercase(), ignoreCase = true)
    }
}
