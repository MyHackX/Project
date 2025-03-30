package com.mtl.My_Hack_X.data.model

import com.google.firebase.Timestamp

data class RegistrationData(
    val id: String = "",
    val userId: String = "",
    val hackathonName: String = "",
    val registrationDate: Timestamp = Timestamp.now(),
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val teamName: String? = null,
    val teamMembers: List<String> = emptyList()
) {
    // Empty constructor for Firestore
    constructor() : this(id = "")
} 