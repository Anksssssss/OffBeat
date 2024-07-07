package com.example.offbeat.models

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
    var offBeatId: String = "",
    var reviewList: MutableList<Review> = mutableListOf()
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
        longitude = ""
    )
}
