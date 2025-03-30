package com.mtl.My_Hack_X.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.mtl.My_Hack_X.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

class AdminRepository(private val firebaseRepository: FirebaseRepository) {
    private val firestore = FirebaseFirestore.getInstance()

    // Admin User Management
    suspend fun promoteToAdmin(userId: String) {
        firestore.collection("users").document(userId)
            .update("isAdmin", true)
            .await()
    }

    suspend fun removeAdminPrivileges(userId: String) {
        firestore.collection("users").document(userId)
            .update("isAdmin", false)
            .await()
    }

    // Batch Operations
    suspend fun deleteUserAndRelatedData(userId: String) {
        val batch = firestore.batch()
        
        // Get user data
        val user = firebaseRepository.getUserById(userId)
        
        if (user != null) {
            // Update events
            user.registeredEvents.forEach { eventId ->
                val eventRef = firestore.collection("events").document(eventId)
                batch.update(eventRef, "registeredUsers", FieldValue.arrayRemove(userId))
                batch.update(eventRef, "currentParticipants", FieldValue.increment(-1))
            }

            // Update hackathons
            user.registeredHackathons.forEach { hackathonId ->
                val hackathonRef = firestore.collection("hackathons").document(hackathonId)
                batch.update(hackathonRef, "registeredUsers", FieldValue.arrayRemove(userId))
                batch.update(hackathonRef, "currentParticipants", FieldValue.increment(-1))
            }

            // Delete user document
            batch.delete(firestore.collection("users").document(userId))
            
            batch.commit().await()
        }
    }

    // Event Management
    suspend fun createEventWithValidation(event: Event): Result<String> {
        return try {
            // Validate event data
            require(event.title.isNotBlank()) { "Event title cannot be empty" }
            require(event.maxParticipants > 0) { "Maximum participants must be greater than 0" }
            
            val eventId = firebaseRepository.createEvent(event)
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hackathon Management
    suspend fun createHackathonWithValidation(hackathon: HackathonEvent): Result<String> {
        return try {
            // Validate hackathon data
            require(hackathon.name.isNotBlank()) { "Hackathon name cannot be empty" }
            require(hackathon.maxParticipants > 0) { "Maximum participants must be greater than 0" }
            require(hackathon.endDate.seconds > hackathon.startDate.seconds) { "End date must be after start date" }
            
            val hackathonId = firebaseRepository.createHackathon(hackathon)
            Result.success(hackathonId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bulk Operations
    suspend fun deleteAllEventsForUser(userId: String) {
        val events = firebaseRepository.getAllEvents().first()
        events.filter { it.organizer == userId }.forEach { event ->
            firebaseRepository.deleteEvent(event.id)
        }
    }

    suspend fun deleteAllHackathonsForUser(userId: String) {
        val hackathons = firebaseRepository.getAllHackathons().first()
        hackathons.filter { it.organizer == userId }.forEach { hackathon ->
            firebaseRepository.deleteHackathon(hackathon.id)
        }
    }

    // User Registration Management
    suspend fun updateUserRegistrations(userId: String, eventId: String, isRegistering: Boolean) {
        val userRef = firestore.collection("users").document(userId)
        val eventRef = firestore.collection("events").document(eventId)
        
        val batch = firestore.batch()
        
        if (isRegistering) {
            batch.update(userRef, "registeredEvents", FieldValue.arrayUnion(eventId))
            batch.update(eventRef, "registeredUsers", FieldValue.arrayUnion(userId))
            batch.update(eventRef, "currentParticipants", FieldValue.increment(1))
        } else {
            batch.update(userRef, "registeredEvents", FieldValue.arrayRemove(eventId))
            batch.update(eventRef, "registeredUsers", FieldValue.arrayRemove(userId))
            batch.update(eventRef, "currentParticipants", FieldValue.increment(-1))
        }
        
        batch.commit().await()
    }

    suspend fun updateHackathonRegistrations(userId: String, hackathonId: String, isRegistering: Boolean) {
        val userRef = firestore.collection("users").document(userId)
        val hackathonRef = firestore.collection("hackathons").document(hackathonId)
        
        val batch = firestore.batch()
        
        if (isRegistering) {
            batch.update(userRef, "registeredHackathons", FieldValue.arrayUnion(hackathonId))
            batch.update(hackathonRef, "registeredUsers", FieldValue.arrayUnion(userId))
            batch.update(hackathonRef, "currentParticipants", FieldValue.increment(1))
        } else {
            batch.update(userRef, "registeredHackathons", FieldValue.arrayRemove(hackathonId))
            batch.update(hackathonRef, "registeredUsers", FieldValue.arrayRemove(userId))
            batch.update(hackathonRef, "currentParticipants", FieldValue.increment(-1))
        }
        
        batch.commit().await()
    }
} 