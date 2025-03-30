package com.mtl.My_Hack_X

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.NavGraph
import com.mtl.My_Hack_X.navigation.Screen
import com.mtl.My_Hack_X.ui.theme.MyHackXTheme
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.model.EventType
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.utils.SampleDataGenerator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.random.Random
import com.mtl.My_Hack_X.screens.RegistrationFormScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Preload sample data in a coroutine scope outside the UI thread
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "Starting to load sample data...")
                val firebaseService = FirebaseService.getInstance()
                
                // Delete any existing events and hackathons to start fresh
                try {
                    val existingEvents = firebaseService.getAllEvents()
                    for (event in existingEvents) {
                        firebaseService.deleteEvent(event.id)
                    }
                    
                    val existingHackathons = firebaseService.getAllHackathons()
                    for (hackathon in existingHackathons) {
                        firebaseService.deleteHackathon(hackathon.id)
                    }
                    Log.d("MainActivity", "Cleared existing events and hackathons")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error clearing existing data", e)
                }
                
                // Always create high-quality events and hackathons on each app start
                withContext(Dispatchers.IO) {
                    createHighQualitySampleData(firebaseService)
                }
                
                Log.d("MainActivity", "Sample data loading complete")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in sample data loading process", e)
            }
        }
        
        setContent {
            MyHackXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val firebaseService = remember { FirebaseService.getInstance() }
                    var startDestination by remember { mutableStateOf<String?>(null) }
                    var isLoading by remember { mutableStateOf(true) }
                    
                    // Determine start destination based on auth state
                    LaunchedEffect(Unit) {
                        startDestination = if (firebaseService.isUserLoggedIn()) {
                            Screen.Home.route
                        } else {
                            Screen.Splash.route  // Start with Splash screen
                        }
                        isLoading = false
                    }
                    
                    if (!isLoading && startDestination != null) {
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun createHighQualitySampleData(firebaseService: FirebaseService) {
        val eventImages = listOf(
            "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format",
            "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=800&auto=format",
            "https://images.unsplash.com/photo-1591115765373-5207764f72e4?w=800&auto=format",
            "https://images.unsplash.com/photo-1523580494863-6f3031224c94?w=800&auto=format",
            "https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=800&auto=format"
        )
        
        val hackathonImages = listOf(
            "https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=800&auto=format",
            "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format",
            "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800&auto=format",
            "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800&auto=format",
            "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800&auto=format"
        )
        
        val eventTitles = listOf(
            "Tech Summit 2023",
            "Mobile DevCon",
            "AI & ML Workshop",
            "Web3 Conference",
            "Cloud Computing Expo"
        )
        
        val hackathonTitles = listOf(
            "CodeFest 2023",
            "Hack the Future",
            "Innovation Challenge",
            "Data Science Hackathon",
            "Quantum Computing Sprint"
        )
        
        val locations = listOf(
            "San Francisco, CA",
            "New York, NY",
            "Boston, MA",
            "Austin, TX",
            "Seattle, WA"
        )
        
        // Create events with proper data
        for (i in 0 until 5) {
            try {
                // Calculate random start date between now and 30 days from now
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, 30))
                val startDate = calendar.time
                
                // End date is 1-3 days after start date
                calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, 4))
                val endDate = calendar.time
                
                val event = Event(
                    id = UUID.randomUUID().toString(),
                    title = eventTitles[i],
                    description = "Join us for this exciting event featuring expert speakers, hands-on workshops, and valuable networking opportunities. Perfect for developers, designers, and tech enthusiasts of all skill levels.",
                    startDate = startDate,
                    endDate = endDate,
                    location = locations[i],
                    maxParticipants = (50..200 step 25).toList()[Random.nextInt(0, 7)],
                    currentParticipants = Random.nextInt(0, 30),
                    organizer = "MyHackX Team",
                    organizerName = "MyHackX Team",
                    imageUrl = eventImages[i],
                    registeredUsers = listOf(),
                    status = EventStatus.UPCOMING,
                    type = EventType.values()[Random.nextInt(EventType.values().size)],
                    requirements = "Laptop and enthusiasm",
                    tags = listOf("Technology", "Learning", "Networking")
                )
                
                val result = firebaseService.createEvent(event)
                Log.d("MainActivity", "Created event: ${result.title} with ID: ${result.id}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to create event at index $i", e)
            }
        }
        
        // Create hackathons with proper data
        for (i in 0 until 5) {
            try {
                // Calculate random start date between now and 60 days from now
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(7, 60))
                val startDate = calendar.time
                
                // End date is 2-3 days after start date
                calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(2, 4))
                val endDate = calendar.time
                
                val hackathon = Hackathon(
                    id = UUID.randomUUID().toString(),
                    title = hackathonTitles[i],
                    description = "This hackathon brings together coders, designers, and problem solvers for a 48-hour coding challenge. Build innovative solutions, win prizes, and meet other talented developers!",
                    startDate = startDate,
                    endDate = endDate,
                    location = locations[(i + 2) % locations.size],
                    maxParticipants = (100..300 step 50).toList()[Random.nextInt(0, 5)],
                    currentParticipants = Random.nextInt(0, 50),
                    prizePool = "$${(5000..20000 step 1000).toList()[Random.nextInt(0, 16)]}",
                    organizer = "MyHackX Team",
                    imageUrl = hackathonImages[i],
                    registeredUsers = listOf(),
                    requirements = "Laptop, coding skills, and a passion for innovation",
                    teams = Random.nextInt(5, 15),
                    teamSize = Random.nextInt(3, 6),
                    status = HackathonStatus.UPCOMING
                )
                
                val result = firebaseService.createHackathon(hackathon)
                Log.d("MainActivity", "Created hackathon: ${result.title} with ID: ${result.id}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to create hackathon at index $i", e)
            }
        }
    }
}