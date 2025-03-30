package com.mtl.My_Hack_X.data

import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.google.firebase.Timestamp
import java.util.Date

object HackathonDataManager {
    private val _hackathons = mutableListOf<HackathonEvent>()
    val hackathons: List<HackathonEvent> = _hackathons

    init {
        // Initialize with sample data
        addSampleHackathons()
    }

    fun addHackathon(hackathon: HackathonEvent) {
        _hackathons.add(hackathon)
    }

    fun removeHackathon(id: String) {
        _hackathons.removeAll { hackathon -> hackathon.id == id }
    }

    fun updateHackathon(hackathon: HackathonEvent) {
        val index = _hackathons.indexOfFirst { existingHackathon -> existingHackathon.id == hackathon.id }
        if (index != -1) {
            _hackathons[index] = hackathon
        }
    }

    private fun addSampleHackathons() {
        val now = Timestamp.now()
        val oneDay = 24 * 60 * 60 * 1000L // one day in milliseconds
        
        _hackathons.addAll(
            listOf(
                HackathonEvent(
                    id = "1",
                    name = "Tech Innovate 2024",
                    description = "48-hour hackathon focused on AI and ML solutions",
                    startDate = Timestamp(Date(now.seconds * 1000 + oneDay)), // tomorrow
                    endDate = Timestamp(Date(now.seconds * 1000 + 3 * oneDay)), // 3 days from now
                    location = "Bangalore",
                    maxParticipants = 100,
                    currentParticipants = 0,
                    registrationDeadline = now, // registration deadline is today
                    prizePool = "₹2,00,000",
                    organizer = "Tech Foundation",
                    requirements = listOf("Laptop", "Android Studio", "Basic Android Knowledge"),
                    tags = listOf("Android", "Kotlin", "Mobile Development"),
                    imageUrl = "https://example.com/image1.jpg",
                    registeredUsers = emptyList()
                ),
                HackathonEvent(
                    id = "2",
                    name = "Code for Change",
                    description = "Build solutions for social impact",
                    startDate = Timestamp(Date(now.seconds * 1000 + 7 * oneDay)), // week from now
                    endDate = Timestamp(Date(now.seconds * 1000 + 8 * oneDay)), // 8 days from now
                    location = "Mumbai",
                    maxParticipants = 150,
                    currentParticipants = 0,
                    registrationDeadline = Timestamp(Date(now.seconds * 1000 + 6 * oneDay)), // deadline in 6 days
                    prizePool = "₹3,00,000",
                    organizer = "Social Tech Foundation",
                    requirements = listOf("Laptop", "Internet Connection", "Team of 2-4"),
                    tags = listOf("Social Impact", "Innovation", "Technology"),
                    imageUrl = "https://example.com/image2.jpg",
                    registeredUsers = emptyList()
                )
            )
        )
    }
} 