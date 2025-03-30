package com.mtl.My_Hack_X.data.model

import java.util.Date

/**
 * Data class representing detailed registration information for events and hackathons.
 */
data class RegistrationDetails(
    val userId: String = "",
    val eventId: String = "",  // Can be eventId or hackathonId depending on the registration type
    val isHackathon: Boolean = false,  // Flag to differentiate between event and hackathon
    val registrationType: RegistrationType = RegistrationType.EVENT,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val college: String = "",
    val currentEducation: String = "",
    val isGraduate: Boolean = false,
    val fieldOfStudy: String = "",
    val yearOfStudy: String = "",
    val teamName: String = "",
    val teamMembers: List<TeamMember> = emptyList(),
    val skills: List<String> = emptyList(),
    val previousExperience: String = "",
    val portfolioLink: String = "",
    val githubLink: String = "",
    val linkedinProfile: String = "",
    val dietaryRestrictions: String = "",
    val tshirtSize: String = "",
    val additionalInfo: String = "",
    val registrationDate: Date = Date(),
    val id: String = ""  // Unique ID for the registration entry
) {
    // Required empty constructor for Firestore
    constructor() : this(id = "")
}

/**
 * Data class representing a team member in a hackathon team.
 */
data class TeamMember(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val college: String = "",
    val role: String = ""
) {
    // Required empty constructor for Firestore
    constructor() : this(name = "")
}

/**
 * Enum representing the type of registration.
 */
enum class RegistrationType {
    EVENT,
    HACKATHON,
    WORKSHOP
} 