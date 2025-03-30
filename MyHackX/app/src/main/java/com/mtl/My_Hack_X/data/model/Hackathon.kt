package com.mtl.My_Hack_X.data.model

import java.util.Date

/**
 * Data class representing a Hackathon.
 */
data class Hackathon(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val location: String = "",
    val prizePool: String = "",
    val imageUrl: String = "",
    val organizerName: String = "",
    val registrationUrl: String = "",
    // Legacy fields to maintain compatibility
    val maxParticipants: Int = 100,
    val currentParticipants: Int = 0,
    val organizer: String = "",
    val registeredUsers: List<String> = listOf(),
    val status: HackathonStatus = HackathonStatus.UPCOMING,
    val themes: List<String> = listOf(),
    val requirements: String = "",
    val sponsors: List<String> = listOf(),
    val judges: List<String> = listOf(),
    val teams: Int = 0,
    val teamSize: Int = 4
) {
    // Required empty constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        startDate = Date(),
        endDate = Date(),
        location = "",
        prizePool = "",
        imageUrl = "",
        organizerName = "",
        registrationUrl = "",
        maxParticipants = 100,
        currentParticipants = 0,
        organizer = "",
        registeredUsers = listOf(),
        status = HackathonStatus.UPCOMING,
        themes = listOf(),
        requirements = "",
        sponsors = listOf(),
        judges = listOf(),
        teams = 0,
        teamSize = 4
    )
}

/**
 * Enum representing different states of a Hackathon.
 */
enum class HackathonStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED
} 