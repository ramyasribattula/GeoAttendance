package com.cutmap.geoattendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(private val list: List<Attendance>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.itemDate)
        val timeText: TextView = view.findViewById(R.id.itemTime)
        val distanceText: TextView = view.findViewById(R.id.itemDistance)
        val addressText: TextView = view.findViewById(R.id.itemAddress)
        val statusText: TextView = view.findViewById(R.id.itemStatus)
        val barcodeText: TextView = view.findViewById(R.id.itemBarcode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.dateText.text = "Date: ${item.date}"
        holder.timeText.text = "Time: ${item.time}"
        holder.distanceText.text = "Distance: ${"%.2f".format(item.distance)} m"
        holder.addressText.text = "Address: ${item.address}"
        holder.statusText.text = "Status: ${item.status}"
        holder.barcodeText.text = "Barcode: ${item.scannedBarcode.ifEmpty { "None" }}"
    }

    override fun getItemCount() = list.size
}
