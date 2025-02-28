package com.example.myhackx.data

import com.example.myhackx.data.models.HackathonEvent

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
        _hackathons.removeAll { it.id == id }
    }

    fun updateHackathon(hackathon: HackathonEvent) {
        val index = _hackathons.indexOfFirst { it.id == hackathon.id }
        if (index != -1) {
            _hackathons[index] = hackathon
        }
    }

    private fun addSampleHackathons() {
        _hackathons.addAll(
            listOf(
                HackathonEvent(
                    id = "1",
                    name = "Tech Innovate 2024",
                    description = "48-hour hackathon focused on AI and ML solutions",
                    startDate = "2024-03-15",
                    endDate = "2024-03-17",
                    location = "Bangalore",
                    imageUrl = "https://example.com/image1.jpg",
                    registrationUrl = "https://example.com/register1",
                    organizer = "Tech Foundation",
                    prizePool = "₹2,00,000"
                ),
                HackathonEvent(
                    id = "2",
                    name = "Code for Change",
                    description = "Build solutions for social impact",
                    startDate = "2024-04-01",
                    endDate = "2024-04-02",
                    location = "Mumbai",
                    imageUrl = "https://example.com/image2.jpg",
                    registrationUrl = "https://example.com/register2",
                    organizer = "Social Tech Foundation",
                    prizePool = "₹3,00,000"
                )
            )
        )
    }
} 