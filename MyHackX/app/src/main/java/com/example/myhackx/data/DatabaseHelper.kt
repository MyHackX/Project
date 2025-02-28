package com.example.myhackx.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.myhackx.data.models.User
import com.example.myhackx.data.models.Registration
import com.example.myhackx.data.models.HackathonEvent

object DatabaseHelper {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

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

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("HackXDB", Context.MODE_PRIVATE)
        loadData()
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
        val userRegistrations = _registrations.value.filter { it.userId == userId }
        return _hackathonFlow.value.filter { hackathon ->
            userRegistrations.any { it.hackathonName == hackathon.name }
        }
    }

    fun getHackathonParticipants(hackathonName: String): List<User> {
        val hackathonRegistrations = _registrations.value.filter { it.hackathonName == hackathonName }
        return _userFlow.value.filter { user ->
            hackathonRegistrations.any { it.userId == user.id }
        }
    }

    fun updateHackathon(hackathon: HackathonEvent) {
        val currentList = _hackathonFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.name == hackathon.name }
        if (index != -1) {
            currentList[index] = hackathon
            _hackathonFlow.value = currentList
            saveData()
        }
    }

    fun deleteHackathon(hackathonName: String) {
        _hackathonFlow.value = _hackathonFlow.value.filter { it.name != hackathonName }
        // Also remove related registrations
        _registrations.value = _registrations.value.filter { it.hackathonName != hackathonName }
        saveData()
    }

    fun addRegistration(registration: RegistrationData) {
        _registrationDetails.value = _registrationDetails.value + registration
        saveData()
    }

    fun isUserRegistered(userId: String, hackathonName: String): Boolean {
        return _registrationDetails.value.any { 
            it.userId == userId && it.hackathonName == hackathonName 
        }
    }
} 