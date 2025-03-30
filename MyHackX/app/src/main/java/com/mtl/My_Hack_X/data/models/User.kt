package com.mtl.My_Hack_X.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAdmin: Boolean = false,
    val registeredEvents: List<String> = listOf()
) {
    fun copy(registeredEvents: List<String>? = null): User {
        return User(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            photoUrl = this.photoUrl,
            isAdmin = this.isAdmin,
            registeredEvents = registeredEvents ?: this.registeredEvents
        )
    }
} 