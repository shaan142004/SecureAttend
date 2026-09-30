package com.secureattend.app.model

/**
 * Represents one student's profile, stored in Firestore under "students/{uid}".
 * NOTE: We do NOT store the actual fingerprint anywhere — fingerprint matching
 * happens entirely on-device via Android's BiometricPrompt / Keystore. We only
 * store a flag confirming that biometric verification succeeded for a session.
 */
data class Student(
    val uid: String = "",
    val name: String = "",
    val rollNumber: String = "",
    val prnNumber: String = "",
    val className: String = "",
    val email: String = "",
    val mobile: String = ""
)
