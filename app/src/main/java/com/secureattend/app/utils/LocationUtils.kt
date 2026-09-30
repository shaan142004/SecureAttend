package com.secureattend.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Gets the student's current GPS location and calculates the distance in
 * metres from the classroom router's registered coordinates. Attendance is
 * only allowed if the student is within Classroom.allowedRadiusMeters (5m).
 *
 * NOTE: Plain GPS accuracy indoors is often 3-8m on its own, which is why
 * this check is combined with the Wi-Fi BSSID check rather than used alone —
 * together they make it very hard to spoof both at once.
 */
object LocationUtils {

    @SuppressLint("MissingPermission") // Caller MUST check permission before calling this
    fun getCurrentLocation(
        context: Context,
        onResult: (Location?) -> Unit
    ) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location -> onResult(location) }
            .addOnFailureListener { onResult(null) }
    }

    /** Returns the distance in metres between the student's location and the router's location. */
    fun distanceToClassroom(
        studentLat: Double,
        studentLng: Double,
        classroomLat: Double,
        classroomLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(studentLat, studentLng, classroomLat, classroomLng, results)
        return results[0] // metres
    }

    /** True if the student is within the allowed radius (default 5m) of the classroom router. */
    fun isWithinClassroomRadius(
        studentLat: Double,
        studentLng: Double,
        classroomLat: Double,
        classroomLng: Double,
        allowedRadiusMeters: Float = 5f
    ): Boolean {
        val distance = distanceToClassroom(studentLat, studentLng, classroomLat, classroomLng)
        return distance <= allowedRadiusMeters
    }
}
