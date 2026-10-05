package com.secureattend.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

/**
 * Returns a stable identifier for this physical device/install, used to
 * "lock" a student account to one phone (device-binding anti-proxy check).
 *
 * ANDROID_ID is unique per app-signing-key + device + user profile, and
 * stays the same across app reinstalls as long as the signing key doesn't
 * change — good enough for this purpose. It resets if the device is
 * factory-reset, which is fine (a teacher can reset deviceId for a student
 * via the "update" rule already in firestore.rules for exactly this case).
 */
object DeviceUtils {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }
}
