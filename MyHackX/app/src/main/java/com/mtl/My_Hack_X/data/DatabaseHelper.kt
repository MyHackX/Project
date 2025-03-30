package com.mtl.My_Hack_X.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.mtl.My_Hack_X.data.model.User
import com.mtl.My_Hack_X.data.model.Registration
import com.mtl.My_Hack_X.data.model.RegistrationData
import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.mtl.My_Hack_X.data.model.Event
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.Timestamp
import java.util.Date
import com.mtl.My_Hack_X.data.repository.FirebaseRepository
import com.mtl.My_Hack_X.data.repository.AdminRepository

object DatabaseHelper {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private const val PREF_NAME = "MyHackXPrefs"

    // StateFlows for live data updates
    private val _userFlow = MutableStateFlow<List<User>>(emptyList())
    val userFlow: StateFlow<List<User>> = _userFlow

    private val _hackathonFlow = MutableStateFlow<List<HackathonEvent>>(emptyList())
    val hackathonFlow: StateFlow<List<HackathonEvent>> = _hackathonFlow

    private val _registrations = MutableStateFlow<List<Registration>>(emptyList())
    val registrations: StateFlow<List<Registration>> = _registrations

    private val _registrationDetails = MutableStateFlow<List<RegistrationData>>(emptyList())
    val registrationDetails: StateFlow<List<RegistrationData>> = _registrationDetails

    // Backing lists for data storage
    private val userList = mutableListOf<User>()
    private val hackathonList = mutableListOf<HackathonEvent>()
    private val registeredEvents = mutableMapOf<String, MutableList<String>>() // userId to list of hackathonIds

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var adminRepository: AdminRepository

    fun initialize(context: Context) {
        // Initialize Firebase
        if (!isFirebaseInitialized()) {
            FirebaseApp.initializeApp(context)
        }

        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        // Initialize repositories
        firebaseRepository = FirebaseRepository()
        adminRepository = AdminRepository(firebaseRepository)

        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadData()
    }

    private fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    // Repository getters
    fun getFirebaseRepository(): FirebaseRepository {
        if (!::firebaseRepository.isInitialized) {
            throw IllegalStateException("DatabaseHelper must be initialized before getting repositories")
        }
        return firebaseRepository
    }

    fun getAdminRepository(): AdminRepository {
        if (!::adminRepository.isInitialized) {
            throw IllegalStateException("DatabaseHelper must be initialized before getting repositories")
        }
        return adminRepository
    }

    // Helper functions for common database operations
    suspend fun isCurrentUserAdmin(uid: String): Boolean {
        return getFirebaseRepository().isUserAdmin(uid)
    }

    suspend fun createInitialAdminUser(user: com.mtl.My_Hack_X.data.model.User) {
        getFirebaseRepository().createUser(user.copy(isAdmin = true))
    }

    // Sample data creation for testing
    suspend fun createSampleData() {
        val sampleEvent = Event(
            title = "Sample Coding Workshop",
            description = "Learn the basics of Android development",
            maxParticipants = 30,
            location = "Virtual"
        )
        
        val now = Timestamp.now()
        val oneDay = 24 * 60 * 60 * 1000L // one day in milliseconds
        
        val sampleHackathon = HackathonEvent(
            name = "Sample Hackathon 2024",
            description = "48-hour coding challenge",
            maxParticipants = 100,
            location = "Virtual",
            prizePool = "$5000",
            startDate = Timestamp(Date(now.seconds * 1000 + oneDay)), // tomorrow
            endDate = Timestamp(Date(now.seconds * 1000 + 3 * oneDay)), // 3 days from now
            registrationDeadline = now, // registration deadline is today
            organizer = "Tech Foundation",
            requirements = listOf("Laptop", "Android Studio", "Basic Android Knowledge"),
            tags = listOf("Android", "Kotlin", "Mobile Development")
        )

        getAdminRepository().createEventWithValidation(sampleEvent)
        getAdminRepository().createHackathonWithValidation(sampleHackathon)
    }

    private fun loadData() {
        // Load Users
        val usersJson = sharedPreferences.getString("users", "[]")
        val userType = object : TypeToken<List<User>>() {}.type
        _userFlow.value = gson.fromJson(usersJson, userType)

        // Load Hackathons
        val hackathonsJson = sharedPreferences.getString("hackathons", "[]")
        val hackathonType = object : TypeToken<List<HackathonEvent>>() {}.type
        _hackathonFlow.value = gson.fromJson(hackathonsJson, hackathonType)

        // Load Registrations
        val registrationsJson = sharedPreferences.getString("registrations", "[]")
        val registrationType = object : TypeToken<List<Registration>>() {}.type
        _registrations.value = gson.fromJson(registrationsJson, registrationType)
    }

    private fun saveData() {
        sharedPreferences.edit().apply {
            putString("users", gson.toJson(_userFlow.value))
            putString("hackathons", gson.toJson(_hackathonFlow.value))
            putString("registrations", gson.toJson(_registrations.value))
            apply()
        }
    }

    fun addUser(user: User): Boolean {
        if (getUserByEmail(user.email) != null) {
            return false // User already exists
        }
        val result = userList.add(user)
        if (result) {
            _userFlow.value = userList.toList()
        }
        return result
    }

    fun getUserByEmail(email: String): User? {
        return userList.find { it.email == email }
    }

    fun addHackathon(hackathon: HackathonEvent): Boolean {
        val result = hackathonList.add(hackathon)
        if (result) {
            _hackathonFlow.value = hackathonList.toList()
        }
        return result
    }

    fun getAllHackathons(): List<HackathonEvent> {
        return hackathonList.toList()
    }

    fun registerForHackathon(userId: String, hackathonId: String): Boolean {
        val userEvents = registeredEvents.getOrPut(userId) { mutableListOf() }
        if (hackathonId in userEvents) {
            return false // Already registered
        }
        userEvents.add(hackathonId)
        return true
    }

    fun getRegisteredHackathons(userId: String): List<HackathonEvent> {
        val userEvents = registeredEvents[userId] ?: return emptyList()
        return hackathonList.filter { it.id in userEvents }
    }

    fun isRegisteredForHackathon(userId: String, hackathonId: String): Boolean {
        return registeredEvents[userId]?.contains(hackathonId) == true
    }

    // Optional: Clear data (useful for testing)
    fun clearAll() {
        userList.clear()
        hackathonList.clear()
        registeredEvents.clear()
        _userFlow.value = emptyList()
        _hackathonFlow.value = emptyList()
    }

    fun getUserRegistrations(userId: String): List<HackathonEvent> {
        val userRegistrations = _registrations.value.filter { registration -> registration.userId == userId }
        return _hackathonFlow.value.filter { hackathon ->
            userRegistrations.any { registration -> registration.hackathonId == hackathon.id }
        }
    }

    fun getHackathonParticipants(hackathonId: String): List<User> {
        val hackathonRegistrations = _registrations.value.filter { registration -> registration.hackathonId == hackathonId }
        return _userFlow.value.filter { user ->
            hackathonRegistrations.any { registration -> registration.userId == user.uid }
        }
    }

    fun updateHackathon(hackathon: HackathonEvent) {
        val currentList = _hackathonFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == hackathon.id }
        if (index != -1) {
            currentList[index] = hackathon
            _hackathonFlow.value = currentList
            saveData()
        }
    }

    fun deleteHackathon(hackathonId: String) {
        _hackathonFlow.value = _hackathonFlow.value.filter { it.id != hackathonId }
        // Also remove related registrations
        _registrations.value = _registrations.value.filter { it.hackathonId != hackathonId }
        saveData()
    }

    fun addRegistration(registration: RegistrationData) {
        val currentList = _registrationDetails.value.toMutableList()
        currentList.add(registration)
        _registrationDetails.value = currentList
        saveData()
    }

    fun isUserRegistered(userId: String, hackathonId: String): Boolean {
        return _registrations.value.any { 
            it.userId == userId && it.hackathonId == hackathonId 
        }
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
} 