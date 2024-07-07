package com.example.offbeat.models

import com.google.firebase.Timestamp

data class Review(
    val postId: String,
    val userName: String,
    val review: String,
    val time: String,
){
    constructor() : this(
        postId = "",
        userName = "",
        review = "",
        time = ""
    )
}

