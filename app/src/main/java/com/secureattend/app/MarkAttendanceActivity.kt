package com.secureattend.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivityMarkAttendanceBinding
import com.secureattend.app.model.AttendanceRecord
import com.secureattend.app.model.Lecture
import com.secureattend.app.utils.BiometricAuthHelper
import com.secureattend.app.utils.FirestoreRepository
import com.secureattend.app.utils.LocationUtils
import com.secureattend.app.utils.WifiUtils

/**
 * Runs the 3-check verification flow for ONE specific lecture (passed in via
 * the "lectureId" intent extra from SelectLectureActivity):
 *   1. Fingerprint match (BiometricAuthHelper)      -> proves WHO
 *   2. Wi-Fi BSSID match to that lecture's router     -> proves WHICH network
 *   3. GPS within that lecture's allowed radius        -> proves HOW CLOSE
 */
class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAttendanceBinding
    private var lecture: Lecture? = null
    private val locationPermissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lectureId = intent.getStringExtra("lectureId")
        if (lectureId == null) {
            setStatus("❌ No lecture selected. Go back and pick a lecture.")
            binding.btnMarkAttendance.isEnabled = false
            return
        }

        binding.tvClassroomName.text = "Loading lecture..."
        FirestoreRepository.getLecture(lectureId) { fetched ->
            if (fetched == null) {
                setStatus("❌ This lecture no longer exists.")
                binding.btnMarkAttendance.isEnabled = false
            } else {
                lecture = fetched
                binding.tvClassroomName.text = "${fetched.subject}\n${fetched.teacherName} · ${fetched.classroomName}"
            }
        }

        binding.btnMarkAttendance.setOnClickListener { startVerificationFlow() }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }

    private fun startVerificationFlow() {
        val currentLecture = lecture
        if (currentLecture == null) {
            setStatus("❌ Lecture not loaded yet. Try again in a moment.")
            return
        }

        setStatus("Checking permissions...")
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode
            )
            return
        }
        step1_fingerprint(currentLecture)
    }

    private fun step1_fingerprint(lecture: Lecture) {
        setStatus("Step 1/3: Verify fingerprint...")
        BiometricAuthHelper.authenticate(
            activity = this,
            onSuccess = {
                setStatus("✅ Fingerprint verified")
                step2_wifi(lecture)
            },
            onError = { message -> setStatus("❌ Fingerprint failed: $message") }
        )
    }

    private fun step2_wifi(lecture: Lecture) {
        setStatus("Step 2/3: Checking classroom Wi-Fi...")
        if (!WifiUtils.isConnectedToClassroomWifi(this, lecture.routerBssid)) {
            setStatus("❌ Not connected to ${lecture.classroomName}'s Wi-Fi. Attendance blocked.")
            return
        }
        setStatus("✅ Wi-Fi verified")
        step3_location(lecture)
    }

    private fun step3_location(lecture: Lecture) {
        setStatus("Step 3/3: Checking your location...")
        LocationUtils.getCurrentLocation(this) { location ->
            if (location == null) {
                setStatus("❌ Could not get GPS location. Turn on location and try again.")
                return@getCurrentLocation
            }

            val distance = LocationUtils.distanceToClassroom(
                location.latitude, location.longitude, lecture.latitude, lecture.longitude
            )

            if (distance > lecture.allowedRadiusMeters) {
                setStatus(
                    "❌ You are %.1fm from the classroom router (must be within %.0fm). Attendance blocked."
                        .format(distance, lecture.allowedRadiusMeters)
                )
                return@getCurrentLocation
            }

            setStatus("✅ Location verified (%.1fm from router)".format(distance))
            saveAttendance(lecture, distance)
        }
    }

    private fun saveAttendance(lecture: Lecture, distance: Float) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirestoreRepository.getStudent(uid) { student ->
            val record = AttendanceRecord(
                studentUid = uid,
                studentName = student?.name ?: "Unknown",
                rollNumber = student?.rollNumber ?: "-",
                lectureId = lecture.lectureId,
                subject = lecture.subject,
                teacherName = lecture.teacherName,
                classroomName = lecture.classroomName,
                fingerprintVerified = true,
                wifiVerified = true,
                locationVerified = true,
                distanceFromRouterMeters = distance,
                deviceId = student?.deviceId ?: ""
            )

            FirestoreRepository.saveAttendanceRecord(record) { success ->
                setStatus(
                    if (success) "🎉 Attendance marked successfully!"
                    else "❌ Failed to save attendance. Check your internet connection."
                )
            }
        }
    }

    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startVerificationFlow()
        } else {
            setStatus("❌ Location permission is required to mark attendance.")
        }
    }
}
