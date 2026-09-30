package com.secureattend.app.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.secureattend.app.model.AttendanceRecord
import com.secureattend.app.model.Lecture
import com.secureattend.app.model.Student

/**
 * All Firestore reads/writes go through here so the Activities stay simple.
 *
 * Firestore structure:
 *   students/{uid}          -> Student
 *   teachers/{uid}          -> { uid, name, email }  (marks an account as faculty)
 *   lectures/{lectureId}    -> Lecture (subject + teacher + room, owned by one teacher)
 *   attendance/{autoId}     -> AttendanceRecord (tagged to one lectureId)
 */
object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- Students ----------------

    fun saveStudent(student: Student, onComplete: (Boolean) -> Unit) {
        db.collection("students").document(student.uid)
            .set(student)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getStudent(uid: String, onResult: (Student?) -> Unit) {
        db.collection("students").document(uid).get()
            .addOnSuccessListener { doc -> onResult(doc.toObject(Student::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    // ---------------- Teacher role check ----------------

    /** True if this uid has a "teachers/{uid}" document (created at website registration). */
    fun isTeacher(uid: String, onResult: (Boolean) -> Unit) {
        db.collection("teachers").document(uid).get()
            .addOnSuccessListener { doc -> onResult(doc.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // ---------------- Lectures ----------------

    fun createLecture(lecture: Lecture, onComplete: (Boolean) -> Unit) {
        val ref = db.collection("lectures").document()
        val withId = lecture.copy(lectureId = ref.id)
        ref.set(withId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateLecture(lecture: Lecture, onComplete: (Boolean) -> Unit) {
        db.collection("lectures").document(lecture.lectureId)
            .set(lecture)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteLecture(lectureId: String, onComplete: (Boolean) -> Unit) {
        db.collection("lectures").document(lectureId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getLecture(lectureId: String, onResult: (Lecture?) -> Unit) {
        db.collection("lectures").document(lectureId).get()
            .addOnSuccessListener { doc -> onResult(doc.toObject(Lecture::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    /** All lectures owned by one teacher (for their "My Lectures" management screen). */
    fun getMyLectures(teacherUid: String, onResult: (List<Lecture>) -> Unit) {
        db.collection("lectures")
            .whereEqualTo("teacherUid", teacherUid)
            .get()
            .addOnSuccessListener { snap -> onResult(snap.toObjects(Lecture::class.java)) }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * ALL lectures across every teacher. Used by the student's "Select Lecture"
     * screen, which then filters client-side (via LectureTimeUtils) to just
     * the ones happening right now. Fine at small college-project scale.
     */
    fun getAllLectures(onResult: (List<Lecture>) -> Unit) {
        db.collection("lectures")
            .get()
            .addOnSuccessListener { snap -> onResult(snap.toObjects(Lecture::class.java)) }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // ---------------- Attendance ----------------

    fun saveAttendanceRecord(record: AttendanceRecord, onComplete: (Boolean) -> Unit) {
        db.collection("attendance")
            .add(record)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /** All attendance records for one lecture, most recent first. */
    fun getAttendanceForLecture(lectureId: String, onResult: (List<AttendanceRecord>) -> Unit) {
        db.collection("attendance")
            .whereEqualTo("lectureId", lectureId)
            .get()
            .addOnSuccessListener { snap ->
                val records = snap.toObjects(AttendanceRecord::class.java)
                    .sortedByDescending { it.timestamp }
                onResult(records)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
