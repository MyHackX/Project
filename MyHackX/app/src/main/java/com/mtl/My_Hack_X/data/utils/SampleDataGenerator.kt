package com.mtl.My_Hack_X.data.utils

import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.model.EventType
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.random.Random

object SampleDataGenerator {
    
    private val eventImages = listOf(
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fevent1.jpg?alt=media",
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fevent2.jpg?alt=media",
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fevent3.jpg?alt=media",
        "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=500&auto=format",
        "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=500&auto=format",
        "https://images.unsplash.com/photo-1591115765373-5207764f72e4?w=500&auto=format"
    )
    
    private val hackathonImages = listOf(
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fhackathon1.jpg?alt=media",
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fhackathon2.jpg?alt=media",
        "https://firebasestorage.googleapis.com/v0/b/my-hack-x-41643.appspot.com/o/sample%2Fhackathon3.jpg?alt=media",
        "https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=500&auto=format",
        "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=500&auto=format",
        "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=500&auto=format"
    )
    
    private val eventTitles = listOf(
        "Tech Innovators Summit",
        "Code Warriors Challenge",
        "Future Tech Expo",
        "AI & Machine Learning Workshop",
        "Web Dev Conference",
        "Mobile App Showcase",
        "Data Science Forum",
        "Blockchain Revolution",
        "DevOps Masterclass",
        "IoT Exploration Day",
        "Cybersecurity Bootcamp",
        "UI/UX Design Sprint"
    )
    
    private val hackathonTitles = listOf(
        "Code for Change",
        "Hack the Future",
        "Innovation Challenge",
        "Dream Builder Hackathon",
        "Global Solution Sprint",
        "Tech for Good",
        "AI Revolution Challenge",
        "Sustainable Dev Hackathon",
        "FinTech Disruptors",
        "Health Tech Innovation",
        "Smart City Hackathon",
        "Quantum Computing Challenge"
    )
    
    private val eventLocations = listOf(
        "Tech Hub, Silicon Valley",
        "Innovation Center, New York",
        "Digital Campus, Boston",
        "Code Space, Austin",
        "Future Lab, Seattle",
        "Developer Zone, Toronto",
        "Byte Conference Hall, London",
        "Tech Park, Berlin",
        "Innovation District, Singapore",
        "Digital Quarter, Tokyo"
    )
    
    private fun getRandomPastDate(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }
    
    private fun getRandomFutureDate(daysAhead: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
        return calendar.time
    }
    
    private fun getRandomEndDate(startDate: Date, maxDurationDays: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, maxDurationDays + 1))
        return calendar.time
    }
    
    private fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }
    
    fun createSampleEvent(index: Int): Event {
        val id = UUID.randomUUID().toString()
        
        // Randomly choose status and dates accordingly
        val statusRandom = Random.nextInt(0, 100)
        val status: EventStatus
        val startDate: Date
        val endDate: Date
        
        when {
            statusRandom < 60 -> { 
                // 60% chance of UPCOMING event
                status = EventStatus.UPCOMING
                startDate = getRandomFutureDate(Random.nextInt(1, 31))
                endDate = getRandomEndDate(startDate, 5)
            }
            statusRandom < 80 -> { 
                // 20% chance of ONGOING event
                status = EventStatus.ONGOING
                startDate = getRandomPastDate(Random.nextInt(1, 3))
                endDate = getRandomFutureDate(Random.nextInt(1, 7))
            }
            else -> { 
                // 20% chance of COMPLETED event
                status = EventStatus.COMPLETED
                startDate = getRandomPastDate(Random.nextInt(10, 60))
                endDate = getRandomPastDate(Random.nextInt(1, 9))
            }
        }
        
        val currentParticipants = if (status == EventStatus.COMPLETED) {
            Random.nextInt(30, 201) // Completed events have more participants
        } else {
            Random.nextInt(0, 51)
        }
        
        val maxParticipants = (50..200 step 10).toList()[Random.nextInt(0, 16)]
        
        val titleIndex = (index % eventTitles.size)
        val title = if (index >= eventTitles.size) {
            "${eventTitles[titleIndex]} ${index + 1}"
        } else {
            eventTitles[titleIndex]
        }
        
        return Event(
            id = id,
            title = title,
            description = "Join us for an exciting tech event with workshops, networking opportunities, and expert speakers. This event focuses on cutting-edge technologies and innovation in the tech industry. Perfect for professionals, students, and tech enthusiasts looking to expand their knowledge and network.",
            startDate = startDate,
            endDate = endDate,
            location = eventLocations[Random.nextInt(eventLocations.size)],
            maxParticipants = maxParticipants,
            currentParticipants = currentParticipants,
            organizer = "MyHackX Team",
            registeredUsers = listOf(),
            imageUrl = eventImages[Random.nextInt(eventImages.size)],
            status = status,
            type = EventType.values()[Random.nextInt(EventType.values().size)],
            requirements = "Laptop, creativity, and enthusiasm",
            tags = listOf("Technology", "Networking", "Learning", "Workshop")
        )
    }
    
    fun createSampleHackathon(index: Int): Hackathon {
        val id = UUID.randomUUID().toString()
        
        // Randomly choose status and dates accordingly
        val statusRandom = Random.nextInt(0, 100)
        val status: HackathonStatus
        val startDate: Date
        val endDate: Date
        
        when {
            statusRandom < 65 -> { 
                // 65% chance of UPCOMING hackathon
                status = HackathonStatus.UPCOMING
                startDate = getRandomFutureDate(Random.nextInt(5, 46))
                endDate = getRandomEndDate(startDate, 3)
            }
            statusRandom < 85 -> { 
                // 20% chance of ONGOING hackathon
                status = HackathonStatus.ONGOING
                startDate = getRandomPastDate(Random.nextInt(1, 2))
                endDate = getRandomFutureDate(Random.nextInt(1, 3))
            }
            else -> { 
                // 15% chance of COMPLETED hackathon
                status = HackathonStatus.COMPLETED
                startDate = getRandomPastDate(Random.nextInt(10, 60))
                endDate = getRandomPastDate(Random.nextInt(1, 9))
            }
        }
        
        val currentParticipants = if (status == HackathonStatus.COMPLETED) {
            Random.nextInt(80, 401) // Completed hackathons have more participants
        } else {
            Random.nextInt(0, 81)
        }
        
        val maxParticipants = (100..500 step 50).toList()[Random.nextInt(0, 9)]
        
        val titleIndex = (index % hackathonTitles.size)
        val title = if (index >= hackathonTitles.size) {
            "${hackathonTitles[titleIndex]} ${index + 1}"
        } else {
            hackathonTitles[titleIndex]
        }
        
        return Hackathon(
            id = id,
            title = title,
            description = "Join this 48-hour coding challenge to solve real-world problems, win amazing prizes, and network with industry experts. Teams will compete to create innovative solutions that address critical challenges in various domains. Perfect for developers, designers, and problem solvers of all skill levels.",
            startDate = startDate,
            endDate = endDate,
            location = eventLocations[Random.nextInt(eventLocations.size)],
            maxParticipants = maxParticipants,
            currentParticipants = currentParticipants,
            prizePool = "$${(1000..10000 step 1000).toList()[Random.nextInt(0, 10)]}",
            organizer = "MyHackX Team",
            registeredUsers = listOf(),
            imageUrl = hackathonImages[Random.nextInt(hackathonImages.size)],
            requirements = "Laptop, basic coding knowledge, enthusiasm for problem-solving",
            teams = Random.nextInt(10, 31),
            teamSize = Random.nextInt(2, 6),
            status = status
        )
    }
    
    suspend fun populateSampleData(firebaseService: FirebaseService): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if data already exists
            val existingEvents = firebaseService.getAllEvents()
            val existingHackathons = firebaseService.getAllHackathons()
            
            // Only populate if there's no data
            if (existingEvents.isEmpty()) {
                // Create 10 sample events
                for (i in 0 until 10) {
                    val event = createSampleEvent(i)
                    firebaseService.createEvent(event)
                }
            }
            
            if (existingHackathons.isEmpty()) {
                // Create 10 sample hackathons
                for (i in 0 until 10) {
                    val hackathon = createSampleHackathon(i)
                    firebaseService.createHackathon(hackathon)
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 