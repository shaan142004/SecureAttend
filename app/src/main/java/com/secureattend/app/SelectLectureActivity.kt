package com.secureattend.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.secureattend.app.databinding.ActivitySelectLectureBinding
import com.secureattend.app.model.Lecture
import com.secureattend.app.utils.FirestoreRepository
import com.secureattend.app.utils.LectureTimeUtils

/**
 * Student-facing screen shown right after login. Fetches every lecture in
 * Firestore, then keeps only the ones currently in their scheduled time
 * window (LectureTimeUtils.isHappeningNow) — so students only ever see
 * lectures that are actually running right now, across all teachers.
 */
class SelectLectureActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectLectureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLectureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRefresh.setOnClickListener { loadLectures() }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        loadLectures()
    }

    override fun onResume() {
        super.onResume()
        loadLectures()
    }

    private fun loadLectures() {
        binding.tvEmpty.text = "Loading lectures..."
        binding.lectureContainer.removeAllViews()

        FirestoreRepository.getAllLectures { allLectures ->
            val activeNow = allLectures.filter { LectureTimeUtils.isHappeningNow(it) }

            if (activeNow.isEmpty()) {
                binding.tvEmpty.text = "No lectures happening right now. Check back at your lecture's scheduled time."
                return@getAllLectures
            }

            binding.tvEmpty.text = ""
            for (lecture in activeNow) {
                addLectureRow(lecture)
            }
        }
    }

    private fun addLectureRow(lecture: Lecture) {
        val row = TextView(this).apply {
            text = "${lecture.subject}\n${lecture.teacherName} · ${lecture.classroomName} · ${lecture.startTime}-${lecture.endTime}"
            textSize = 15f
            gravity = Gravity.START
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFF1F3F4.toInt())
            setTextColor(0xFF202124.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setOnClickListener {
                val intent = Intent(this@SelectLectureActivity, MarkAttendanceActivity::class.java)
                intent.putExtra("lectureId", lecture.lectureId)
                startActivity(intent)
            }
        }
        binding.lectureContainer.addView(row)
    }
}
