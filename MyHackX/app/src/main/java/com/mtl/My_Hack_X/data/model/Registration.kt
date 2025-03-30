package com.mtl.My_Hack_X.data.model

import com.google.firebase.Timestamp
import com.mtl.My_Hack_X.data.model.HackathonEvent

data class Registration(
    val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val registrationDate: Timestamp = Timestamp.now(),
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val teamName: String? = null,
    val teamMembers: List<String> = emptyList()
) {
    // Empty constructor for Firestore
    constructor() : this(id = "")
}

enum class RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WAITLISTED
} 