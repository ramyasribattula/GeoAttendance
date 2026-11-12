package com.cutmap.geoattendance

data class Attendance(
    val date: String = "",
    val time: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distance: Float = 0f,
    val address: String = "",
    val status: String = "",
    val scannedBarcode: String = ""
)
