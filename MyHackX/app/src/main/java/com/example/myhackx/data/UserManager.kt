package com.example.myhackx.data

import com.example.myhackx.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateListOf
import com.example.myhackx.data.models.HackathonEvent

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String? = null,
    val isAdmin: Boolean = false,
    val profilePicture: String? = null,
    val registeredEvents: List<String> = emptyList()
)

object UserManager {
    private val _users = mutableStateListOf<User>()
    val users = _users

    private const val ADMIN_EMAIL = "mtlmistry123@gmail.com"
    private const val ADMIN_PASSWORD = "12345678"
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun init(databaseHelper: DatabaseHelper) {
        DatabaseHelper.addUser(
            User(
                name = "MTL Admin",
                email = ADMIN_EMAIL,
                password = ADMIN_PASSWORD,
                isAdmin = true
            )
        )
    }

    fun registerUser(name: String, email: String, password: String): Boolean {
        val user = User(
            name = name,
            email = email,
            password = password,
            isAdmin = false
        )
        return DatabaseHelper.addUser(user)
    }

    fun login(email: String, password: String): Boolean {
        val user = DatabaseHelper.getUserByEmail(email)
        return if (user != null && user.password == password) {
            setCurrentUser(user)
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getUserRegistrations(): List<HackathonEvent> {
        return _currentUser.value?.let { user ->
            DatabaseHelper.getUserRegistrations(user.id)
        } ?: emptyList()
    }

    fun registerForEvent(userId: String, eventId: String): Boolean {
        val userIndex = _users.indexOfFirst { it.id == userId }
        if (userIndex != -1) {
            val user = _users[userIndex]
            if (!user.registeredEvents.contains(eventId)) {
                _users[userIndex] = user.copy(
                    registeredEvents = user.registeredEvents + eventId
                )
                return true
            }
        }
        return false
    }

    fun isAdmin(email: String): Boolean {
        return email == ADMIN_EMAIL
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }
} 