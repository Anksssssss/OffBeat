package com.example.offbeat.models

data class OffbeatDetail(
    val userId: String,
    val name: String,
    val description: String,
    val stayDuration: String="",
    val bestTime: String="",
    val address: String,
    val photos: MutableList<String>,
    val latitude: String = "",
    val longitude: String = ""
)
