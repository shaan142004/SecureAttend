package com.secureattend.app.model

/**
 * One attendance entry, stored in Firestore under "attendance/{autoId}".
 * CHANGED: now tagged to a specific Lecture (subject + teacher + room)
 * instead of a single hardcoded classroom.
 */
data class AttendanceRecord(
    val studentUid: String = "",
    val studentName: String = "",
    val rollNumber: String = "",
    val lectureId: String = "",
    val subject: String = "",
    val teacherName: String = "",
    val classroomName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val fingerprintVerified: Boolean = false,
    val wifiVerified: Boolean = false,
    val locationVerified: Boolean = false,
    val distanceFromRouterMeters: Float = -1f
)
