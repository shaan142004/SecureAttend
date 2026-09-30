package com.secureattend.app.utils

import com.secureattend.app.model.Lecture
import java.util.Calendar

/**
 * Determines whether a Lecture is happening right now, based on its
 * dayOfWeek + startTime/endTime, compared against the device's current
 * day and time. Used to show students only relevant, currently-running
 * lectures instead of every lecture ever created.
 */
object LectureTimeUtils {

    fun isHappeningNow(lecture: Lecture): Boolean {
        val now = Calendar.getInstance()

        val todayAbbrev = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }

        if (lecture.dayOfWeek != "Daily" && lecture.dayOfWeek != todayAbbrev) {
            return false
        }

        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = toMinutes(lecture.startTime)
        val end = toMinutes(lecture.endTime)

        return if (start <= end) {
            nowMinutes in start..end
        } else {
            nowMinutes >= start || nowMinutes <= end
        }
    }

    private fun toMinutes(hhmm: String): Int {
        val parts = hhmm.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hours * 60 + minutes
    }
}
