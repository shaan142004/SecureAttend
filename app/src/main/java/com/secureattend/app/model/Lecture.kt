package com.secureattend.app.model

/**
 * One recurring lecture slot, created and owned by a teacher.
 * Stored in Firestore under "lectures/{lectureId}".
 *
 * dayOfWeek: "Mon","Tue","Wed","Thu","Fri","Sat","Sun", or "Daily"
 * startTime / endTime: 24-hour "HH:mm" strings, e.g. "09:00", "10:00"
 */
data class Lecture(
    val lectureId: String = "",
    val teacherUid: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val dayOfWeek: String = "Daily",
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val classroomName: String = "",
    val routerBssid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val allowedRadiusMeters: Float = 5f
)
