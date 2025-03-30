package com.mtl.My_Hack_X.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.mtl.My_Hack_X.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Collection references
    private val usersCollection = firestore.collection("users")
    private val eventsCollection = firestore.collection("events")
    private val hackathonsCollection = firestore.collection("hackathons")
    private val registrationsCollection = firestore.collection("registrations")

    // User Operations
    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun updateUser(user: User) {
        usersCollection.document(user.uid).set(user, SetOptions.merge()).await()
    }

    suspend fun deleteUser(uid: String) {
        usersCollection.document(uid).delete().await()
    }

    suspend fun getUserById(uid: String): User? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(User::class.java)
    }

    fun getAllUsers(): Flow<List<User>> = flow {
        val snapshot = usersCollection.get().await()
        emit(snapshot.toObjects(User::class.java))
    }

    // Event Operations
    suspend fun createEvent(event: Event): String {
        val docRef = eventsCollection.document()
        val eventWithId = event.copy(id = docRef.id)
        docRef.set(eventWithId).await()
        return docRef.id
    }

    suspend fun updateEvent(event: Event) {
        eventsCollection.document(event.id).set(event, SetOptions.merge()).await()
    }

    suspend fun deleteEvent(eventId: String) {
        eventsCollection.document(eventId).delete().await()
    }

    fun getEvent(eventId: String): Flow<Event?> = flow {
        val snapshot = eventsCollection.document(eventId).get().await()
        emit(snapshot.toObject(Event::class.java))
    }

    fun getAllEvents(): Flow<List<Event>> = flow {
        val snapshot = eventsCollection.get().await()
        emit(snapshot.toObjects(Event::class.java))
    }

    // Hackathon Operations
    suspend fun createHackathon(hackathon: HackathonEvent): String {
        val docRef = hackathonsCollection.document()
        val hackathonWithId = hackathon.copy(id = docRef.id)
        docRef.set(hackathonWithId).await()
        return docRef.id
    }

    suspend fun updateHackathon(hackathon: HackathonEvent) {
        hackathonsCollection.document(hackathon.id).set(hackathon, SetOptions.merge()).await()
    }

    suspend fun deleteHackathon(hackathonId: String) {
        hackathonsCollection.document(hackathonId).delete().await()
    }

    suspend fun getHackathonById(hackathonId: String): HackathonEvent? {
        val snapshot = hackathonsCollection.document(hackathonId).get().await()
        return snapshot.toObject(HackathonEvent::class.java)
    }

    fun getAllHackathons(): Flow<List<HackathonEvent>> = flow {
        val snapshot = hackathonsCollection.get().await()
        emit(snapshot.toObjects(HackathonEvent::class.java))
    }

    // Registration Operations
    suspend fun registerForHackathon(hackathonId: String, userId: String) {
        val batch = firestore.batch()
        
        val userRef = usersCollection.document(userId)
        val hackathonRef = hackathonsCollection.document(hackathonId)
        
        batch.update(userRef, "registeredHackathons", com.google.firebase.firestore.FieldValue.arrayUnion(hackathonId))
        batch.update(hackathonRef, "registeredUsers", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
        batch.update(hackathonRef, "currentParticipants", com.google.firebase.firestore.FieldValue.increment(1))
        
        batch.commit().await()
    }

    suspend fun unregisterFromHackathon(hackathonId: String, userId: String) {
        val batch = firestore.batch()
        
        val userRef = usersCollection.document(userId)
        val hackathonRef = hackathonsCollection.document(hackathonId)
        
        batch.update(userRef, "registeredHackathons", com.google.firebase.firestore.FieldValue.arrayRemove(hackathonId))
        batch.update(hackathonRef, "registeredUsers", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
        batch.update(hackathonRef, "currentParticipants", com.google.firebase.firestore.FieldValue.increment(-1))
        
        batch.commit().await()
    }

    suspend fun getUserRegistrations(userId: String): List<HackathonEvent> {
        val user = getUserById(userId) ?: return emptyList()
        val registeredHackathons = user.registeredHackathons ?: return emptyList()
        
        return registeredHackathons.mapNotNull { hackathonId ->
            getHackathonById(hackathonId)
        }
    }

    suspend fun isUserRegisteredForHackathon(hackathonId: String, userId: String): Boolean {
        val hackathon = getHackathonById(hackathonId) ?: return false
        return hackathon.registeredUsers.contains(userId)
    }

    // Admin Operations
    suspend fun isUserAdmin(uid: String): Boolean {
        val userDoc = usersCollection.document(uid).get().await()
        return userDoc.toObject(User::class.java)?.isAdmin ?: false
    }
} 