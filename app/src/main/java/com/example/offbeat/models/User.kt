package com.example.offbeat.models

data class User(
    val id: String,
    val name: String,
    val email: String
){
    constructor() : this(
        id = "",
        name = "",
        email = ""
    )
}
