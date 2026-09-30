package com.secureattend.app.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps Android's BiometricPrompt so MarkAttendanceActivity can just call
 * BiometricAuthHelper.authenticate(activity) { success -> ... }
 *
 * This uses the phone's OWN fingerprint sensor (already enrolled by the
 * student in their phone's Settings > Security > Fingerprint). We never
 * capture or store the raw fingerprint — the OS handles matching in secure
 * hardware and just tells us "match" or "no match".
 */
object BiometricAuthHelper {

    /** Call this first to check the phone actually supports fingerprint auth. */
    fun isFingerprintAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Fingerprint read but did not match enrolled student — do NOT
                // mark attendance. BiometricPrompt keeps letting them retry.
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Your Identity")
            .setSubtitle("Scan your fingerprint to mark attendance")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(promptInfo)
    }
}
