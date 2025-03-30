package com.mtl.My_Hack_X.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val profilePictureUrl: String = "",
    
    @get:PropertyName("admin")
    @set:PropertyName("admin")
    @PropertyName("admin")
    var isAdmin: Boolean = false,
    
    val registeredEvents: List<String> = emptyList(),
    val registeredHackathons: List<String> = emptyList(),
    val id: String = ""
) {
    // Empty constructor for Firestore
    constructor() : this(uid = "")
} 