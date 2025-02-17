package com.example.offbeat.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class OffbeatDetail(
    val userId: String,
    val userName:String="",
    val locationName: String,
    val description: String,
    val stayDuration: String="",
    val bestTime: String="",
    val address: String,
    val photos: MutableList<String>,
    val latitude: String = "",
    val longitude: String = "",
    val state: String,
    val city: String,
    var offBeatId: String = "",
    var reviewList: MutableList<Review> = mutableListOf(),
    @ServerTimestamp val timestamp: Date? = null
){
    constructor() : this(
        userId = "",
        userName = "",
        locationName = "",
        description = "",
        stayDuration = "",
        bestTime = "",
        address = "",
        photos = mutableListOf(),
        latitude = "",
        longitude = "",
        state = "",
        city = ""
    )
}
