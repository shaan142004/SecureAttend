package com.secureattend.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivityLectureFormBinding
import com.secureattend.app.model.Lecture
import com.secureattend.app.utils.FirestoreRepository
import com.secureattend.app.utils.LocationUtils
import com.secureattend.app.utils.WifiUtils

/**
 * ONE screen used for both creating a new lecture and editing an existing
 * one. If the "lectureId" intent extra is present, it's edit mode (loads +
 * shows Delete); otherwise it's a fresh, blank "Add Lecture" form.
 *
 * This REPLACES the old ClassroomSettingsActivity from the previous update —
 * delete that file if you still have it from before.
 */
class LectureFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLectureFormBinding
    private var editingLectureId: String? = null
    private val days = listOf("Daily", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val locationPermissionRequestCode = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLectureFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)

        editingLectureId = intent.getStringExtra("lectureId")
        if (editingLectureId != null) {
            binding.tvTitle.text = "Edit Lecture"
            binding.btnSave.text = "Update Lecture"
            binding.btnDelete.visibility = android.view.View.VISIBLE
            loadExistingLecture(editingLectureId!!)
        } else {
            binding.tvTitle.text = "Add Lecture"
        }

        binding.btnUseCurrentWifi.setOnClickListener { useCurrentWifi() }
        binding.btnUseCurrentLocation.setOnClickListener { useCurrentLocation() }
        binding.btnSave.setOnClickListener { saveLecture() }
        binding.btnDelete.setOnClickListener { deleteLecture() }
    }

    private fun loadExistingLecture(lectureId: String) {
        FirestoreRepository.getLecture(lectureId) { lecture ->
            if (lecture == null) return@getLecture
            binding.etSubject.setText(lecture.subject)
            binding.spinnerDay.setSelection(days.indexOf(lecture.dayOfWeek).coerceAtLeast(0))
            binding.etStartTime.setText(lecture.startTime)
            binding.etEndTime.setText(lecture.endTime)
            binding.etClassroomName.setText(lecture.classroomName)
            binding.etBssid.setText(lecture.routerBssid)
            binding.etLat.setText(lecture.latitude.toString())
            binding.etLng.setText(lecture.longitude.toString())
            binding.etRadius.setText(lecture.allowedRadiusMeters.toString())
        }
    }

    private fun useCurrentWifi() {
        val bssid = WifiUtils.getCurrentBssid(this)
        if (bssid == null) {
            binding.tvStatus.text = "❌ Not connected to Wi-Fi, or Location permission not granted."
        } else {
            binding.etBssid.setText(bssid)
            binding.tvStatus.text = "✅ Filled in current Wi-Fi BSSID."
        }
    }

    private fun useCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode
            )
            return
        }
        binding.tvStatus.text = "Getting current location..."
        LocationUtils.getCurrentLocation(this) { location ->
            if (location == null) {
                binding.tvStatus.text = "❌ Could not get GPS location."
            } else {
                binding.etLat.setText(location.latitude.toString())
                binding.etLng.setText(location.longitude.toString())
                binding.tvStatus.text = "✅ Filled in current location."
            }
        }
    }

    private fun saveLecture() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val teacherName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Teacher"

        val subject = binding.etSubject.text.toString().trim()
        val day = days[binding.spinnerDay.selectedItemPosition]
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()
        val classroomName = binding.etClassroomName.text.toString().trim()
        val bssid = binding.etBssid.text.toString().trim()
        val lat = binding.etLat.text.toString().toDoubleOrNull()
        val lng = binding.etLng.text.toString().toDoubleOrNull()
        val radius = binding.etRadius.text.toString().toFloatOrNull()

        if (subject.isEmpty() || start.isEmpty() || end.isEmpty() || classroomName.isEmpty() ||
            bssid.isEmpty() || lat == null || lng == null || radius == null
        ) {
            binding.tvStatus.text = "❌ Please fill all fields with valid values (times as HH:mm, e.g. 09:00)."
            return
        }

        val lecture = Lecture(
            lectureId = editingLectureId ?: "",
            teacherUid = uid,
            teacherName = teacherName,
            subject = subject,
            dayOfWeek = day,
            startTime = start,
            endTime = end,
            classroomName = classroomName,
            routerBssid = bssid,
            latitude = lat,
            longitude = lng,
            allowedRadiusMeters = radius
        )

        binding.tvStatus.text = "Saving..."
        if (editingLectureId != null) {
            FirestoreRepository.updateLecture(lecture) { success ->
                if (success) finish() else binding.tvStatus.text = "❌ Failed to save."
            }
        } else {
            FirestoreRepository.createLecture(lecture) { success ->
                if (success) finish() else binding.tvStatus.text = "❌ Failed to save."
            }
        }
    }

    private fun deleteLecture() {
        val id = editingLectureId ?: return
        binding.tvStatus.text = "Deleting..."
        FirestoreRepository.deleteLecture(id) { success ->
            if (success) finish() else binding.tvStatus.text = "❌ Failed to delete."
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            useCurrentLocation()
        }
    }
}
