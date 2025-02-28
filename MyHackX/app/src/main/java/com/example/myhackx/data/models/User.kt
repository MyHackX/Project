package com.example.myhackx.data.models

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val email: String,
    val password: String,
    val isAdmin: Boolean = false,
    val registeredEvents: List<String> = emptyList(),
    val registrationDate: Long = System.currentTimeMillis()
) {
    fun copy(registeredEvents: List<String>? = null): User {
        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            password = this.password,
            isAdmin = this.isAdmin,
            registeredEvents = registeredEvents ?: this.registeredEvents,
            registrationDate = this.registrationDate
        )
    }
} 