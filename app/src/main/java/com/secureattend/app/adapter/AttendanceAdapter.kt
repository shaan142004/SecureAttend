package com.secureattend.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.secureattend.app.databinding.ItemAttendanceRowBinding
import com.secureattend.app.model.AttendanceRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceAdapter(
    private val records: List<AttendanceRecord>
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAttendanceRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        holder.binding.tvName.text = "${record.studentName} (${record.rollNumber})"
        holder.binding.tvTime.text = timeFormat.format(Date(record.timestamp))
        holder.binding.tvDistance.text = "%.1fm from router".format(record.distanceFromRouterMeters)
    }

    override fun getItemCount(): Int = records.size
}
