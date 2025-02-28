package com.example.myhackx.data.models

data class HackathonEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val startDate: String,
    val endDate: String,
    val location: String,
    val prizePool: String,
    val description: String = "",
    val registrationOpen: Boolean = true,
    val imageUrl: String = "",
    val registrationUrl: String = "",
    val organizer: String = ""
) 