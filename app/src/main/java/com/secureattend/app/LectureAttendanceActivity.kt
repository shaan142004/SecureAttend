package com.secureattend.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.secureattend.app.adapter.AttendanceAdapter
import com.secureattend.app.databinding.ActivityLectureAttendanceBinding
import com.secureattend.app.utils.FirestoreRepository

class LectureAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLectureAttendanceBinding
    private var lectureId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLectureAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lectureId = intent.getStringExtra("lectureId")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.btnRefresh.setOnClickListener { loadAttendance() }
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, LectureFormActivity::class.java)
            intent.putExtra("lectureId", lectureId)
            startActivity(intent)
        }

        loadLectureInfo()
        loadAttendance()
    }

    override fun onResume() {
        super.onResume()
        loadLectureInfo()
        loadAttendance()
    }

    private fun loadLectureInfo() {
        val id = lectureId ?: return
        FirestoreRepository.getLecture(id) { lecture ->
            if (lecture != null) {
                binding.tvLectureTitle.text = "${lecture.subject} — ${lecture.dayOfWeek} ${lecture.startTime}-${lecture.endTime}"
            }
        }
    }

    private fun loadAttendance() {
        val id = lectureId ?: return
        binding.tvEmpty.text = "Loading..."
        FirestoreRepository.getAttendanceForLecture(id) { records ->
            binding.tvEmpty.text = if (records.isEmpty()) "No attendance marked for this lecture yet." else ""
            binding.recyclerView.adapter = AttendanceAdapter(records)
        }
    }
}
