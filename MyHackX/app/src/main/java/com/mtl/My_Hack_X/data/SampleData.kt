package com.example.myhackx.data

import com.example.myhackx.data.models.Hackathon
import java.util.UUID

object SampleData {
    val sampleHackathons = listOf(
        Hackathon(
            id = UUID.randomUUID().toString(),
            title = "AI Innovation Hackathon",
            description = "Build the future of AI with this exciting hackathon",
            startDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000, // 7 days from now
            endDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000, // 10 days from now
            location = "Virtual",
            imageUrl = "https://example.com/ai-hackathon.jpg",
            organizerId = "org123",
            maxParticipants = 100,
            currentParticipants = 45,
            prizePool = "$10,000",
            registrationDeadline = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000 // 5 days from now
        ),
        Hackathon(
            id = UUID.randomUUID().toString(),
            title = "Web3 Development Challenge",
            description = "Create innovative blockchain solutions",
            startDate = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000, // 14 days from now
            endDate = System.currentTimeMillis() + 17 * 24 * 60 * 60 * 1000, // 17 days from now
            location = "Hybrid",
            imageUrl = "https://example.com/web3-hackathon.jpg",
            organizerId = "org456",
            maxParticipants = 150,
            currentParticipants = 78,
            prizePool = "$15,000",
            registrationDeadline = System.currentTimeMillis() + 12 * 24 * 60 * 60 * 1000 // 12 days from now
        )
    )
} 