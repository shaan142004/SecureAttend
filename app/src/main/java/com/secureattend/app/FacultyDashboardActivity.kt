package com.secureattend.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivityFacultyDashboardBinding
import com.secureattend.app.model.Lecture
import com.secureattend.app.utils.FirestoreRepository

/**
 * Faculty home screen. Lists only lectures owned by the signed-in teacher.
 * Tap a lecture to view its attendance; "+ Add Lecture" creates a new one.
 */
class FacultyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacultyDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacultyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRefresh.setOnClickListener { loadLectures() }
        binding.btnAddLecture.setOnClickListener {
            startActivity(Intent(this, LectureFormActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLectures()
    }

    private fun loadLectures() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.tvEmpty.text = "Loading..."
        binding.lectureContainer.removeAllViews()

        FirestoreRepository.getMyLectures(uid) { lectures ->
            if (lectures.isEmpty()) {
                binding.tvEmpty.text = "You haven't added any lectures yet. Tap \"+ Add Lecture\" to create one."
            } else {
                binding.tvEmpty.text = ""
                for (lecture in lectures) addLectureRow(lecture)
            }
        }
    }

    private fun addLectureRow(lecture: Lecture) {
        val row = TextView(this).apply {
            text = "${lecture.subject}\n${lecture.dayOfWeek} · ${lecture.startTime}-${lecture.endTime} · ${lecture.classroomName}\nTap to view attendance / edit"
            textSize = 15f
            gravity = Gravity.START
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFF1F3F4.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setOnClickListener {
                val intent = Intent(this@FacultyDashboardActivity, LectureAttendanceActivity::class.java)
                intent.putExtra("lectureId", lecture.lectureId)
                startActivity(intent)
            }
        }
        binding.lectureContainer.addView(row)
    }
}
