package com.mtl.My_Hack_X.data.models

data class HackathonEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val organizer: String = "",
    val maxParticipants: Int = 100,
    val currentParticipants: Int = 0,
    val registrationDeadline: String = "",
    val status: String = "Upcoming", // Upcoming, Ongoing, Completed
    val imageUrl: String = "",
    val registeredUsers: List<String> = listOf()
) 