package com.example.myhackx.data.models

data class Hackathon(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val location: String = "",
    val imageUrl: String = "",
    val organizerId: String = "",
    val maxParticipants: Int = 0,
    val currentParticipants: Int = 0,
    val prizePool: String = "",
    val registrationDeadline: Long = 0,
    val status: String = "upcoming", // "upcoming", "ongoing", "completed", "cancelled"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 