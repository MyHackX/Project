package com.mtl.My_Hack_X.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.model.EventType
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.model.User
import com.mtl.My_Hack_X.data.services.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Random
import java.util.Calendar

class AdminViewModel(
    private val firebaseService: FirebaseService = FirebaseService.getInstance()
) : ViewModel() {
    
    companion object {
        private const val TAG = "AdminViewModel"
    }
    
    // Events state
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events
    
    private var _hackathons = MutableStateFlow<List<Hackathon>>(emptyList())
    val hackathons: StateFlow<List<Hackathon>> = _hackathons
    
    private var _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Message handling
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadEvents()
                loadHackathons()
                loadUsers()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun loadEvents() {
        try {
            Log.d(TAG, "Loading events...")
            val eventsList = firebaseService.getAllEvents()
            Log.d(TAG, "Loaded ${eventsList.size} events from Firestore")
            _events.value = eventsList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading events", e)
            _errorMessage.value = "Failed to load events: ${e.message}"
        }
    }
    
    suspend fun loadHackathons() {
        try {
            Log.d(TAG, "Loading hackathons...")
            val hackathonsList = firebaseService.getAllHackathons()
            Log.d(TAG, "Loaded ${hackathonsList.size} hackathons from Firestore")
            _hackathons.value = hackathonsList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading hackathons", e)
            _errorMessage.value = "Failed to load hackathons: ${e.message}"
        }
    }
    
    suspend fun loadUsers() {
        try {
            Log.d(TAG, "Loading users...")
            val usersList = firebaseService.getAllUsers()
            Log.d(TAG, "Loaded ${usersList.size} users from Firestore")
            _users.value = usersList
        } catch (e: Exception) {
            Log.e(TAG, "Error loading users", e)
            _errorMessage.value = "Failed to load users: ${e.message}"
        }
    }
    
    fun createEvent(
        title: String, 
        description: String,
        startDate: Date,
        endDate: Date,
        location: String,
        maxParticipants: Int,
        organizer: String,
        imageUrl: String
    ) {
        if (title.isBlank() || description.isBlank()) {
            _errorMessage.value = "Title and description are required"
            return
        }
        
        // Use default image URL if none provided
        val finalImageUrl = if (imageUrl.isBlank()) {
            "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format"
        } else {
            imageUrl
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Creating event: $title")
                val event = Event(
                    id = "",
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    location = location,
                    maxParticipants = maxParticipants,
                    currentParticipants = 0,
                    organizer = organizer,
                    organizerName = organizer,
                    imageUrl = finalImageUrl,
                    registeredUsers = listOf(),
                    status = EventStatus.UPCOMING,
                    type = EventType.WORKSHOP,
                    requirements = "No specific requirements",
                    tags = listOf("Workshop", "Technology")
                )
                
                Log.d(TAG, "Event object created: $event")
                val createdEvent = firebaseService.createEvent(event)
                Log.d(TAG, "Event successfully created with ID: ${createdEvent.id}")
                
                // Refresh the events list
                delay(500) // Short delay before reloading
                loadEvents()
                
                // Clear error message on success
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error creating event", e)
                _errorMessage.value = "Failed to create event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createHackathon(
        title: String, 
        description: String,
        startDate: Date,
        endDate: Date,
        location: String,
        maxParticipants: Int,
        prizePool: String,
        organizer: String,
        imageUrl: String
    ) {
        if (title.isBlank() || description.isBlank()) {
            _errorMessage.value = "Title and description are required"
            return
        }
        
        // Use default image URL if none provided
        val finalImageUrl = if (imageUrl.isBlank()) {
            "https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=800&auto=format"
        } else {
            imageUrl
        }
        
        // Use default prize pool if none provided
        val finalPrizePool = if (prizePool.isBlank()) {
            "$10,000"
        } else {
            prizePool
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Creating hackathon: $title")
                val hackathon = Hackathon(
                    id = "",
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    location = location,
                    maxParticipants = maxParticipants,
                    currentParticipants = 0,
                    prizePool = finalPrizePool,
                    organizer = organizer,
                    imageUrl = finalImageUrl,
                    registeredUsers = listOf(),
                    requirements = "Laptop, creativity, and enthusiasm!",
                    teams = 0,
                    teamSize = 3,
                    status = HackathonStatus.UPCOMING
                )
                
                Log.d(TAG, "Hackathon object created: $hackathon")
                val createdHackathon = firebaseService.createHackathon(hackathon)
                Log.d(TAG, "Hackathon successfully created with ID: ${createdHackathon.id}")
                
                // Refresh the hackathons list
                delay(500) // Short delay before reloading
                loadHackathons()
                
                // Clear error message on success
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error creating hackathon", e)
                _errorMessage.value = "Failed to create hackathon: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to delete event with ID: $eventId")
                val success = firebaseService.deleteEvent(eventId)
                if (success) {
                    Log.d("AdminViewModel", "Event deleted successfully")
                    loadEvents()
                } else {
                    Log.w("AdminViewModel", "Failed to delete event, returned false")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting event: ${e.message}", e)
            }
        }
    }
    
    fun deleteHackathon(hackathonId: String) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to delete hackathon with ID: $hackathonId")
                val success = firebaseService.deleteHackathon(hackathonId)
                if (success) {
                    Log.d("AdminViewModel", "Hackathon deleted successfully")
                    loadHackathons()
                } else {
                    Log.w("AdminViewModel", "Failed to delete hackathon, returned false")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting hackathon: ${e.message}", e)
            }
        }
    }
    
    suspend fun promoteToAdmin(userId: String) {
        _isLoading.value = true
        Log.d(TAG, "Attempting to promote user $userId to admin")
        
        try {
            // Check if the current user is the master admin
            val currentUserEmail = firebaseService.getCurrentUserEmail()
            
            if (currentUserEmail == "mistry.mitul1@gmail.com") {
                Log.d(TAG, "Current user is master admin, proceeding with promotion")
                val success = firebaseService.promoteUserToAdmin(userId)
                if (success) {
                    Log.d(TAG, "User promoted to admin successfully")
                    _message.value = "User promoted to admin successfully"
                    loadUsers() // Refresh user list
                } else {
                    Log.e(TAG, "Failed to promote user to admin")
                    _message.value = "Failed to promote user to admin"
                }
            } else {
                Log.e(TAG, "Only the master admin (mistry.mitul1@gmail.com) can promote users")
                _message.value = "Failed to promote user: Only the master admin can promote users"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error promoting user to admin: ${e.message}", e)
            _message.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun removeAdminPrivileges(userId: String) {
        _isLoading.value = true
        Log.d(TAG, "Attempting to remove admin privileges from user $userId")
        
        try {
            // Check if the current user is the master admin
            val currentUserEmail = firebaseService.getCurrentUserEmail()
            
            if (currentUserEmail == "mistry.mitul1@gmail.com") {
                Log.d(TAG, "Current user is master admin, proceeding with demotion")
                val success = firebaseService.demoteUserFromAdmin(userId)
                if (success) {
                    Log.d(TAG, "Admin privileges removed successfully")
                    _message.value = "Admin privileges removed successfully"
                    loadUsers() // Refresh user list
                } else {
                    Log.e(TAG, "Failed to remove admin privileges")
                    _message.value = "Failed to remove admin privileges"
                }
            } else {
                Log.e(TAG, "Only the master admin (mistry.mitul1@gmail.com) can remove admin privileges")
                _message.value = "Failed to demote user: Only the master admin can demote users"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing admin privileges: ${e.message}", e)
            _message.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    suspend fun updateEvent(event: Event) {
        _isLoading.value = true
        Log.d(TAG, "Updating event: ${event.id}")
        
        try {
            val success = firebaseService.updateEvent(event)
            if (success) {
                Log.d(TAG, "Event updated successfully")
                _message.value = "Event updated successfully"
            } else {
                Log.e(TAG, "Failed to update event")
                _message.value = "Failed to update event"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event: ${e.message}", e)
            _message.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateHackathon(hackathon: Hackathon) {
        _isLoading.value = true
        Log.d(TAG, "Updating hackathon: ${hackathon.id}")
        
        try {
            val success = firebaseService.updateHackathon(hackathon)
            if (success) {
                Log.d(TAG, "Hackathon updated successfully")
                _message.value = "Hackathon updated successfully"
            } else {
                Log.e(TAG, "Failed to update hackathon")
                _message.value = "Failed to update hackathon"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating hackathon: ${e.message}", e)
            _message.value = "Error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
} 