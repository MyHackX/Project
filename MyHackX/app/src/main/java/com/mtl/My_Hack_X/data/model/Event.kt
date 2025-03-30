package com.mtl.My_Hack_X.data.model

import java.util.Date

/**
 * Data class representing an Event.
 */
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val location: String = "",
    val imageUrl: String = "",
    val organizerName: String = "",
    val registrationUrl: String = "",
    // Legacy fields to maintain compatibility
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val maxParticipants: Int = 100,
    val currentParticipants: Int = 0,
    val organizer: String = "",
    val registeredUsers: List<String> = listOf(),
    val status: EventStatus = EventStatus.UPCOMING,
    val type: EventType = EventType.WORKSHOP,
    val requirements: String = "",
    val tags: List<String> = listOf()
) {
    // Required empty constructor for Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        date = Date(),
        location = "",
        imageUrl = "",
        organizerName = "",
        registrationUrl = "",
        startDate = Date(),
        endDate = Date(),
        maxParticipants = 100,
        currentParticipants = 0,
        organizer = "",
        registeredUsers = listOf(),
        status = EventStatus.UPCOMING,
        type = EventType.WORKSHOP,
        requirements = "",
        tags = listOf()
    )
}

/**
 * Enum representing different states of an Event.
 */
enum class EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED
}

/**
 * Enum representing different types of Events.
 */
enum class EventType {
    WORKSHOP,
    WEBINAR,
    HACKATHON,
    CONFERENCE,
    NETWORKING,
    OTHER
} 