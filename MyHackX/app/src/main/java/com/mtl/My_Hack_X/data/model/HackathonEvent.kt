package com.mtl.My_Hack_X.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class HackathonEvent(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val location: String = "",
    val maxParticipants: Int = 0,
    val currentParticipants: Int = 0,
    val registrationDeadline: Timestamp = Timestamp.now(),
    val prizePool: String = "",
    val organizer: String = "",
    val requirements: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageUrl: String = "",
    val registeredUsers: List<String> = emptyList(),
    val status: HackathonStatus = HackathonStatus.UPCOMING
) {
    // Empty constructor for Firestore
    constructor() : this(id = "")
}

// HackathonStatus enum is now defined in Hackathon.kt 