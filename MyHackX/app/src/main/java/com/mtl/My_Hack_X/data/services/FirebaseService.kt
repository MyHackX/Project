package com.mtl.My_Hack_X.data.services

import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mtl.My_Hack_X.data.model.User
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.delay
import com.mtl.My_Hack_X.data.model.RegistrationDetails
import com.mtl.My_Hack_X.data.model.RegistrationType

class FirebaseService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    
    // Collection references
    private val usersCollection = firestore.collection("users")
    private val eventsCollection = firestore.collection("events")
    private val hackathonsCollection = firestore.collection("hackathons")
    private val registrationsCollection = firestore.collection("registrations")

    // Authentication methods
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
    
    suspend fun getCurrentUserProfile(): User? = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.d(TAG, "getCurrentUserProfile: No user is currently logged in")
                return@withContext null
            }
            
            try {
                val documentSnapshot = usersCollection.document(userId).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                
                if (user == null) {
                    Log.w(TAG, "getCurrentUserProfile: User document not found for ID: $userId")
                    
                    // Create a basic profile if user auth exists but no Firestore document
                    val email = getCurrentUserEmail() ?: ""
                    if (email.isNotEmpty()) {
                        Log.d(TAG, "getCurrentUserProfile: Initializing user document for: $email")
                        val displayName = auth.currentUser?.displayName ?: ""
                        val photoUrl = auth.currentUser?.photoUrl?.toString() ?: ""
                        
                        val newUser = User(
                            uid = userId,
                            id = userId,
                            email = email,
                            displayName = displayName,
                            name = displayName,
                            photoUrl = photoUrl,
                            profilePictureUrl = photoUrl,
                            isAdmin = email == "mistry.mitul1@gmail.com",
                            registeredEvents = emptyList(),
                            registeredHackathons = emptyList()
                        )
                        
                        usersCollection.document(userId).set(newUser).await()
                        return@withContext newUser
                    }
                }
                
                return@withContext user
            } catch (e: Exception) {
                Log.e(TAG, "getCurrentUserProfile: Error fetching user data", e)
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentUserProfile: Unexpected error", e)
            return@withContext null
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to sign in user: $email")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
            
            if (userId != null) {
                // Initialize user profile if it doesn't exist
                var user = getUserById(userId)
                if (user == null) {
                    Log.d(TAG, "User profile not found, initializing for: $email")
                    initNewUser(userId, email)
                    user = getUserById(userId)
                }
                
                // Always ensure master admin account has admin privileges
                if (email == "mistry.mitul1@gmail.com") {
                    if (user != null && !user.isAdmin) {
                        Log.d(TAG, "Setting admin privileges for master admin account: $email")
                        val updatedUser = user.copy(isAdmin = true)
                        updateUserProfile(updatedUser)
                    } else if (user == null) {
                        Log.w(TAG, "Failed to retrieve user profile after initialization")
                    }
                }
                
                Log.d(TAG, "Sign in successful for: $email")
                Result.success(userId)
            } else {
                Log.e(TAG, "Authentication succeeded but user ID is null")
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createUser(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating new user account: $email")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User created but user ID is null")
            
            // Initialize user document in Firestore
            val success = initNewUser(uid, email)
            if (!success) {
                Log.w(TAG, "User account created but failed to initialize Firestore document")
            }
            
            Log.d(TAG, "User created successfully: $email with ID: $uid")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getSignInIntent(context: Context): Intent {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("426989300388-1rmohq6d5qn8a53g3cjjmu29u5gm35ds.apps.googleusercontent.com") // Updated Web Client ID
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            return googleSignInClient.signInIntent
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Google Sign In Intent: ${e.message}", e)
            throw e
        }
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Result<String> = withContext(Dispatchers.IO) {
        if (data == null) {
            Log.e(TAG, "Google Sign In failed: Intent data is null")
            return@withContext Result.failure(Exception("Google Sign In was canceled"))
        }
        
        try {
            Log.d(TAG, "Processing Google Sign In result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            
            if (!task.isSuccessful) {
                val exception = task.exception ?: Exception("Unknown error during Google Sign In")
                Log.e(TAG, "Google Sign In task unsuccessful: ${exception.message}", exception)
                return@withContext Result.failure(exception)
            }
            
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign In successful for: ${account.email}")
                
                if (account.email.isNullOrEmpty()) {
                    Log.e(TAG, "Google account email is null or empty")
                    return@withContext Result.failure(Exception("Google account email is missing"))
                }
                
            val idToken = account.idToken
                if (idToken == null) {
                    Log.e(TAG, "ID token is null")
                    return@withContext Result.failure(Exception("Failed to get ID token from Google"))
                }
                
                Log.d(TAG, "Authenticating with Firebase using Google credential")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
                
                try {
            val authResult = auth.signInWithCredential(credential).await()
                    
                    val user = authResult.user
                    if (user == null) {
                        Log.e(TAG, "Firebase user is null after successful authentication")
                        return@withContext Result.failure(Exception("Authentication successful but user is null"))
                    }
                    
                    val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                    Log.d(TAG, "User ${if (isNewUser) "is new" else "already exists"}: ${user.uid}")
                    
                    if (isNewUser) {
                        // Create user profile for new Google sign-in users
                        Log.d(TAG, "Creating new user profile for: ${user.email}")
                        val newUser = User(
                            id = user.uid,
                            uid = user.uid,
                            name = user.displayName ?: "",
                            displayName = user.displayName ?: "",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            profilePictureUrl = user.photoUrl?.toString() ?: "",
                            registeredEvents = listOf(),
                            registeredHackathons = listOf(),
                            isAdmin = user.email == "mistry.mitul1@gmail.com"
                        )
                        val profileCreated = createUserProfile(newUser)
                        if (!profileCreated) {
                            Log.w(TAG, "Failed to create user profile in Firestore, retrying once")
                            // Retry once
                            delay(1000)
                            createUserProfile(newUser)
                        }
                    } else {
                        // Check if profile needs updating for existing users
                        val existingProfile = getUserById(user.uid)
                        if (existingProfile != null && 
                            (existingProfile.photoUrl != user.photoUrl?.toString() || 
                             existingProfile.name != user.displayName)) {
                            Log.d(TAG, "Updating existing user profile with latest Google info")
                            val updatedUser = existingProfile.copy(
                                name = user.displayName ?: existingProfile.name,
                                displayName = user.displayName ?: existingProfile.displayName,
                                photoUrl = user.photoUrl?.toString() ?: existingProfile.photoUrl,
                                profilePictureUrl = user.photoUrl?.toString() ?: existingProfile.profilePictureUrl
                            )
                            updateUserProfile(updatedUser)
                        }
                    }
                    
                    Log.d(TAG, "Google Sign In complete, returning user ID: ${user.uid}")
                    return@withContext Result.success(user.uid)
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase authentication error: ${e.message}", e)
                    return@withContext Result.failure(Exception("Firebase authentication failed: ${e.message}"))
                }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Google Sign In was cancelled"
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during Google Sign In"
                    else -> "Google Sign In failed: ${e.statusCode} - ${e.message}"
                }
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Google Sign In: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
    
    // User methods
    suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun createUserProfile(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating user profile for ${user.email}")
            
            // Ensure user has necessary fields initialized
            val userWithDefaults = user.copy(
                registeredEvents = user.registeredEvents.ifEmpty { emptyList() },
                registeredHackathons = user.registeredHackathons.ifEmpty { emptyList() },
                isAdmin = user.email == "mistry.mitul1@gmail.com" || user.isAdmin
            )
            
            // Check if user document already exists
            val existingUser = getUserById(user.id)
            if (existingUser != null) {
                Log.d(TAG, "User profile already exists, merging data")
                // Merge data rather than replace completely
                val mergedUser = existingUser.copy(
                    name = user.name.ifBlank { existingUser.name },
                    email = user.email.ifBlank { existingUser.email },
                    profilePictureUrl = user.profilePictureUrl.ifBlank { existingUser.profilePictureUrl },
                    isAdmin = user.email == "mistry.mitul1@gmail.com" || existingUser.isAdmin
                )
                usersCollection.document(user.id).set(mergedUser, SetOptions.merge()).await()
            } else {
                // Create new user
                usersCollection.document(user.id).set(userWithDefaults).await()
            }
            
            Log.d(TAG, "User profile created/updated successfully for ${user.email}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user profile: ${e.message}", e)
            false
        }
    }
    
    suspend fun updateUserProfile(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating user profile for ${user.id} (${user.email})")
            
            // First, verify current user has permission to update this profile
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Cannot update user profile - no user is logged in")
                return@withContext false
            }
            
            // For admin changes, check special conditions
            val existingUser = getUserById(user.id)
            if (existingUser != null && existingUser.isAdmin != user.isAdmin) {
                // Admin status is being changed, enforce special security
                Log.d(TAG, "Admin status change detected (${existingUser.isAdmin} -> ${user.isAdmin})")
                
                // Validate that this is the special admin account or an already-admin user
                val currentUserProfile = getUserById(currentUser.uid)
                val hasPermission = (user.email == "mistry.mitul1@gmail.com" || 
                                    (currentUserProfile != null && currentUserProfile.isAdmin))
                
                if (!hasPermission) {
                    Log.e(TAG, "Permission denied: Only the special admin account can be promoted/demoted")
                    return@withContext false
                }
            }
            
            // Proceed with update
            usersCollection.document(user.id).set(user, SetOptions.merge()).await()
            Log.d(TAG, "User profile updated successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}", e)
            return@withContext false
        }
    }
    
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = usersCollection.get().await()
            querySnapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Event methods
    suspend fun createEvent(event: Event): Event = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating event: ${event.title}")
            
            val eventWithId = if (event.id.isBlank()) {
                val newId = eventsCollection.document().id
                Log.d(TAG, "Generated new event ID: $newId")
                event.copy(id = newId)
            } else {
                Log.d(TAG, "Using provided event ID: ${event.id}")
                event
            }
            
            Log.d(TAG, "Setting event in Firestore with ID: ${eventWithId.id}")
            eventsCollection.document(eventWithId.id).set(eventWithId).await()
            Log.d(TAG, "Event successfully created in Firestore: ${eventWithId.id}")
            
            return@withContext eventWithId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun getEventById(eventId: String): Event? = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = eventsCollection.document(eventId).get().await()
            documentSnapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateEvent(event: Event): Boolean = withContext(Dispatchers.IO) {
        try {
            eventsCollection.document(event.id).set(event, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First, get the event to check for registered users
            val event = getEventById(eventId) ?: return@withContext false
            
            // Start a batch to handle multiple operations
            val batch = firestore.batch()
            
            // Remove event from all registered users
            for (userId in event.registeredUsers) {
                val userRef = usersCollection.document(userId)
                batch.update(userRef, "registeredEvents", FieldValue.arrayRemove(eventId))
            }
            
            // Delete the event document
            batch.delete(eventsCollection.document(eventId))
            
            // Commit the batch
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getAllEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = eventsCollection.orderBy("startDate", Query.Direction.ASCENDING).get().await()
            querySnapshot.toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getUpcomingEvents(limit: Long = 3): List<Event> = withContext(Dispatchers.IO) {
        try {
            val currentDate = Date()
            val querySnapshot = eventsCollection
                .whereGreaterThanOrEqualTo("startDate", currentDate)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .limit(limit)
                .get()
                .await()
            
            querySnapshot.toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getEventsByIds(eventIds: List<String>): List<Event> = withContext(Dispatchers.IO) {
        try {
            if (eventIds.isEmpty()) return@withContext emptyList()
            
            // Firestore limitations require us to fetch in batches of 10
            val allEvents = mutableListOf<Event>()
            for (batch in eventIds.chunked(10)) {
                val querySnapshot = eventsCollection.whereIn("id", batch).get().await()
                allEvents.addAll(querySnapshot.toObjects(Event::class.java))
            }
            
            allEvents
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Hackathon methods
    suspend fun createHackathon(hackathon: Hackathon): Hackathon = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating hackathon: ${hackathon.title}")
            
            val hackathonWithId = if (hackathon.id.isBlank()) {
                val newId = hackathonsCollection.document().id
                Log.d(TAG, "Generated new hackathon ID: $newId")
                hackathon.copy(id = newId)
            } else {
                Log.d(TAG, "Using provided hackathon ID: ${hackathon.id}")
                hackathon
            }
            
            Log.d(TAG, "Setting hackathon in Firestore with ID: ${hackathonWithId.id}")
            hackathonsCollection.document(hackathonWithId.id).set(hackathonWithId).await()
            Log.d(TAG, "Hackathon successfully created in Firestore: ${hackathonWithId.id}")
            
            return@withContext hackathonWithId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating hackathon: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun getHackathonById(hackathonId: String): Hackathon? = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = hackathonsCollection.document(hackathonId).get().await()
            documentSnapshot.toObject(Hackathon::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateHackathon(hackathon: Hackathon): Boolean = withContext(Dispatchers.IO) {
        try {
            hackathonsCollection.document(hackathon.id).set(hackathon, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteHackathon(hackathonId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First, get the hackathon to check for registered users
            val hackathon = getHackathonById(hackathonId) ?: return@withContext false
            
            // Start a batch to handle multiple operations
            val batch = firestore.batch()
            
            // Remove hackathon from all registered users
            for (userId in hackathon.registeredUsers) {
                val userRef = usersCollection.document(userId)
                batch.update(userRef, "registeredHackathons", FieldValue.arrayRemove(hackathonId))
            }
            
            // Delete the hackathon document
            batch.delete(hackathonsCollection.document(hackathonId))
            
            // Commit the batch
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getAllHackathons(): List<Hackathon> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = hackathonsCollection.orderBy("startDate", Query.Direction.ASCENDING).get().await()
            querySnapshot.toObjects(Hackathon::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getUpcomingHackathons(limit: Long = 3): List<Hackathon> = withContext(Dispatchers.IO) {
        try {
            val currentDate = Date()
            val querySnapshot = hackathonsCollection
                .whereGreaterThanOrEqualTo("startDate", currentDate)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .limit(limit)
                .get()
                .await()
            
            querySnapshot.toObjects(Hackathon::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getHackathonsByIds(hackathonIds: List<String>): List<Hackathon> = withContext(Dispatchers.IO) {
        try {
            if (hackathonIds.isEmpty()) return@withContext emptyList()
            
            // Firestore limitations require us to fetch in batches of 10
            val allHackathons = mutableListOf<Hackathon>()
            for (batch in hackathonIds.chunked(10)) {
                val querySnapshot = hackathonsCollection.whereIn("id", batch).get().await()
                allHackathons.addAll(querySnapshot.toObjects(Hackathon::class.java))
            }
            
            allHackathons
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Registration methods with improved error handling and logging
    suspend fun registerUserForEvent(userId: String, eventId: String, registrationDetails: RegistrationDetails? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting registration for user $userId to event $eventId")
            
            // Verify user and event exist
            val user = getUserById(userId)
            val event = getEventById(eventId)
            
            if (user == null) {
                Log.e(TAG, "Registration failed: User $userId not found")
                return@withContext false
            }
            
            if (event == null) {
                Log.e(TAG, "Registration failed: Event $eventId not found")
                return@withContext false
            }
            
            // Check if already registered
            if (user.registeredEvents.contains(eventId)) {
                Log.w(TAG, "User $userId is already registered for event $eventId")
                return@withContext true
            }
            
            // Check if event has capacity
            if (event.maxParticipants != null && event.currentParticipants >= event.maxParticipants) {
                Log.e(TAG, "Registration failed: Event $eventId is at maximum capacity")
                return@withContext false
            }

            val batch = firestore.batch()
            
            // Add eventId to user's registeredEvents
            val userRef = usersCollection.document(userId)
            batch.update(userRef, "registeredEvents", FieldValue.arrayUnion(eventId))
            Log.d(TAG, "Adding event $eventId to user $userId registeredEvents")
            
            // Add userId to event's registeredUsers and increment participant count
            val eventRef = eventsCollection.document(eventId)
            batch.update(eventRef, "registeredUsers", FieldValue.arrayUnion(userId))
            batch.update(eventRef, "currentParticipants", FieldValue.increment(1))
            Log.d(TAG, "Adding user $userId to event $eventId registeredUsers and incrementing participants")
            
            batch.commit().await()
            Log.d(TAG, "Successfully registered user $userId for event $eventId")
            
            // Save registration details if provided
            if (registrationDetails != null) {
                val completeDetails = registrationDetails.copy(
                    userId = userId,
                    eventId = eventId,
                    isHackathon = false,
                    registrationType = RegistrationType.EVENT
                )
                saveRegistrationDetails(completeDetails)
                Log.d(TAG, "Saved detailed registration information for event")
            }
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user $userId for event $eventId: ${e.message}", e)
            return@withContext false
        }
    }
    
    suspend fun unregisterUserFromEvent(userId: String, eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting unregistration for user $userId from event $eventId")
            
            // Verify user and event exist
            val user = getUserById(userId)
            val event = getEventById(eventId)
            
            if (user == null) {
                Log.e(TAG, "Unregistration failed: User $userId not found")
                return@withContext false
            }
            
            if (event == null) {
                Log.e(TAG, "Unregistration failed: Event $eventId not found")
                return@withContext false
            }
            
            // Check if actually registered
            if (!user.registeredEvents.contains(eventId)) {
                Log.w(TAG, "User $userId is not registered for event $eventId")
                return@withContext true // Return success since the end state is as desired
            }

            try {
                // First attempt: Use batch operations (more efficient but can fail if there are conflicts)
                Log.d(TAG, "Attempting batch operation for unregistration")
                val batch = firestore.batch()
                
                // Remove eventId from user's registeredEvents
                val userRef = usersCollection.document(userId)
                batch.update(userRef, "registeredEvents", FieldValue.arrayRemove(eventId))
                
                // Remove userId from event's registeredUsers and decrement participant count
                val eventRef = eventsCollection.document(eventId)
                batch.update(eventRef, "registeredUsers", FieldValue.arrayRemove(userId))
                batch.update(eventRef, "currentParticipants", FieldValue.increment(-1))
                
                batch.commit().await()
                Log.d(TAG, "Successfully unregistered user $userId from event $eventId using batch")
                return@withContext true
            } catch (e: Exception) {
                // If batch fails, try individual operations as fallback
                Log.w(TAG, "Batch operation failed, trying individual updates: ${e.message}")
                
                try {
                    // Update user document directly
                    val updatedUserEvents = user.registeredEvents.filter { it != eventId }
                    val userUpdate = hashMapOf<String, Any>(
                        "registeredEvents" to updatedUserEvents
                    )
                    usersCollection.document(userId).update(userUpdate).await()
                    Log.d(TAG, "Updated user document to remove event $eventId")
                    
                    // Update event document directly
                    val updatedEventUsers = event.registeredUsers.filter { it != userId }
                    val eventUpdate = hashMapOf<String, Any>(
                        "registeredUsers" to updatedEventUsers,
                        "currentParticipants" to updatedEventUsers.size
                    )
                    eventsCollection.document(eventId).update(eventUpdate).await()
                    Log.d(TAG, "Updated event document to remove user $userId")
                    
                    return@withContext true
                } catch (e2: Exception) {
                    Log.e(TAG, "Both batch and individual updates failed: ${e2.message}", e2)
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering user $userId from event $eventId: ${e.message}", e)
            return@withContext false
        }
    }
    
    suspend fun registerUserForHackathon(userId: String, hackathonId: String, registrationDetails: RegistrationDetails? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting registration for user $userId to hackathon $hackathonId")
            
            // Verify user and hackathon exist
            val user = getUserById(userId)
            val hackathon = getHackathonById(hackathonId)
            
            if (user == null) {
                Log.e(TAG, "Registration failed: User $userId not found")
                return@withContext false
            }
            
            if (hackathon == null) {
                Log.e(TAG, "Registration failed: Hackathon $hackathonId not found")
                return@withContext false
            }
            
            // Check if already registered
            if (user.registeredHackathons.contains(hackathonId)) {
                Log.w(TAG, "User $userId is already registered for hackathon $hackathonId")
                return@withContext true
            }
            
            // Check if hackathon has capacity
            if (hackathon.maxParticipants != null && hackathon.currentParticipants >= hackathon.maxParticipants) {
                Log.e(TAG, "Registration failed: Hackathon $hackathonId is at maximum capacity")
                return@withContext false
            }

            val batch = firestore.batch()
            
            // Add hackathonId to user's registeredHackathons
            val userRef = usersCollection.document(userId)
            batch.update(userRef, "registeredHackathons", FieldValue.arrayUnion(hackathonId))
            Log.d(TAG, "Adding hackathon $hackathonId to user $userId registeredHackathons")
            
            // Add userId to hackathon's registeredUsers and increment participant count
            val hackathonRef = hackathonsCollection.document(hackathonId)
            batch.update(hackathonRef, "registeredUsers", FieldValue.arrayUnion(userId))
            batch.update(hackathonRef, "currentParticipants", FieldValue.increment(1))
            Log.d(TAG, "Adding user $userId to hackathon $hackathonId registeredUsers and incrementing participants")
            
            batch.commit().await()
            Log.d(TAG, "Successfully registered user $userId for hackathon $hackathonId")
            
            // Save registration details if provided
            if (registrationDetails != null) {
                val completeDetails = registrationDetails.copy(
                    userId = userId,
                    eventId = hackathonId,
                    isHackathon = true,
                    registrationType = RegistrationType.HACKATHON
                )
                saveRegistrationDetails(completeDetails)
                Log.d(TAG, "Saved detailed registration information for hackathon")
            }
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user $userId for hackathon $hackathonId: ${e.message}", e)
            return@withContext false
        }
    }
    
    suspend fun unregisterUserFromHackathon(userId: String, hackathonId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting unregistration for user $userId from hackathon $hackathonId")
            
            // Verify user and hackathon exist
            val user = getUserById(userId)
            val hackathon = getHackathonById(hackathonId)
            
            if (user == null) {
                Log.e(TAG, "Unregistration failed: User $userId not found")
                return@withContext false
            }
            
            if (hackathon == null) {
                Log.e(TAG, "Unregistration failed: Hackathon $hackathonId not found")
                return@withContext false
            }
            
            // Check if actually registered
            if (!user.registeredHackathons.contains(hackathonId)) {
                Log.w(TAG, "User $userId is not registered for hackathon $hackathonId")
                return@withContext true // Return success since the end state is as desired
            }

            try {
                // First attempt: Use batch operations (more efficient but can fail if there are conflicts)
                Log.d(TAG, "Attempting batch operation for unregistration")
                val batch = firestore.batch()
                
                // Remove hackathonId from user's registeredHackathons
                val userRef = usersCollection.document(userId)
                batch.update(userRef, "registeredHackathons", FieldValue.arrayRemove(hackathonId))
                
                // Remove userId from hackathon's registeredUsers and decrement participant count
                val hackathonRef = hackathonsCollection.document(hackathonId)
                batch.update(hackathonRef, "registeredUsers", FieldValue.arrayRemove(userId))
                batch.update(hackathonRef, "currentParticipants", FieldValue.increment(-1))
                
                batch.commit().await()
                Log.d(TAG, "Successfully unregistered user $userId from hackathon $hackathonId using batch")
                return@withContext true
            } catch (e: Exception) {
                // If batch fails, try individual operations as fallback
                Log.w(TAG, "Batch operation failed, trying individual updates: ${e.message}")
                
                try {
                    // Update user document directly
                    val updatedUserHackathons = user.registeredHackathons.filter { it != hackathonId }
                    val userUpdate = hashMapOf<String, Any>(
                        "registeredHackathons" to updatedUserHackathons
                    )
                    usersCollection.document(userId).update(userUpdate).await()
                    Log.d(TAG, "Updated user document to remove hackathon $hackathonId")
                    
                    // Update hackathon document directly
                    val updatedHackathonUsers = hackathon.registeredUsers.filter { it != userId }
                    val hackathonUpdate = hashMapOf<String, Any>(
                        "registeredUsers" to updatedHackathonUsers,
                        "currentParticipants" to updatedHackathonUsers.size
                    )
                    hackathonsCollection.document(hackathonId).update(hackathonUpdate).await()
                    Log.d(TAG, "Updated hackathon document to remove user $userId")
                    
                    return@withContext true
                } catch (e2: Exception) {
                    Log.e(TAG, "Both batch and individual updates failed: ${e2.message}", e2)
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering user $userId from hackathon $hackathonId: ${e.message}", e)
            return@withContext false
        }
    }
    
    // Storage methods
    suspend fun uploadImage(imageUri: Uri, storagePath: String): String = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.reference.child(storagePath)
            val uploadTask = storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun uploadEventImage(eventId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val path = "events/$eventId/image.jpg"
            val imageUrl = uploadImage(imageUri, path)
            
            // Update event with new image URL
            eventsCollection.document(eventId).update("imageUrl", imageUrl).await()
            
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun uploadHackathonImage(hackathonId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val path = "hackathons/$hackathonId/image.jpg"
            val imageUrl = uploadImage(imageUri, path)
            
            // Update hackathon with new image URL
            hackathonsCollection.document(hackathonId).update("imageUrl", imageUrl).await()
            
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val path = "users/$userId/profile.jpg"
            val imageUrl = uploadImage(imageUri, path)
            
            // Update user with new profile picture URL
            usersCollection.document(userId).update("profilePictureUrl", imageUrl).await()
            
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }

    // Admin promotion method - enhanced for better security and special handling
    suspend fun promoteCurrentUserToAdmin(): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "promoteCurrentUserToAdmin: No user is logged in")
                return@withContext false
            }
            
            val user = getUserById(userId)
            if (user == null) {
                Log.e(TAG, "promoteCurrentUserToAdmin: Failed to get user profile for $userId")
                return@withContext false
            }
            
            // Special case: The master admin account always gets verified/promoted
            if (user.email == "mistry.mitul1@gmail.com") {
                if (user.isAdmin) {
                    Log.d(TAG, "Master admin account already has admin privileges")
                    return@withContext true
                }
                
                Log.d(TAG, "Granting admin privileges to master admin account")
                val updatedUser = user.copy(isAdmin = true)
                val success = updateUserProfile(updatedUser)
                
                if (success) {
                    Log.d(TAG, "Successfully granted admin privileges to master admin")
                    return@withContext true
                } else {
                    Log.e(TAG, "Failed to update master admin privileges")
                    return@withContext false
                }
            } else {
                Log.w(TAG, "Only the master admin account can be automatically verified")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying admin status: ${e.message}", e)
            return@withContext false
        }
    }
    
    // Method to allow master admin to promote other users to admin
    suspend fun promoteUserToAdmin(userIdToPromote: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First verify the current user is the master admin
            val currentUserId = getCurrentUserId()
            if (currentUserId == null) {
                Log.e(TAG, "promoteUserToAdmin: No user is logged in")
                return@withContext false
            }
            
            val currentUser = getUserById(currentUserId)
            if (currentUser == null || currentUser.email != "mistry.mitul1@gmail.com") {
                Log.e(TAG, "promoteUserToAdmin: Only the master admin can promote other users")
                return@withContext false
            }
            
            // Now promote the target user
            val userToPromote = getUserById(userIdToPromote)
            if (userToPromote == null) {
                Log.e(TAG, "promoteUserToAdmin: Target user not found")
                return@withContext false
            }
            
            if (userToPromote.isAdmin) {
                Log.d(TAG, "User ${userToPromote.email} is already an admin")
                return@withContext true
            }
            
            Log.d(TAG, "Master admin promoting ${userToPromote.email} to admin")
            val updatedUser = userToPromote.copy(isAdmin = true)
            val success = updateUserProfile(updatedUser)
            
            Log.d(TAG, "Admin promotion ${if (success) "succeeded" else "failed"}")
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error promoting user to admin: ${e.message}", e)
            return@withContext false
        }
    }
    
    // Method to allow master admin to demote other users from admin
    suspend fun demoteUserFromAdmin(userIdToDemote: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First verify the current user is the master admin
            val currentUserId = getCurrentUserId()
            if (currentUserId == null) {
                Log.e(TAG, "demoteUserFromAdmin: No user is logged in")
                return@withContext false
            }
            
            val currentUser = getUserById(currentUserId)
            if (currentUser == null || currentUser.email != "mistry.mitul1@gmail.com") {
                Log.e(TAG, "demoteUserFromAdmin: Only the master admin can demote other users")
                return@withContext false
            }
            
            // Protect the master admin account from demotion
            val userToDemote = getUserById(userIdToDemote)
            if (userToDemote == null) {
                Log.e(TAG, "demoteUserFromAdmin: Target user not found")
                return@withContext false
            }
            
            if (userToDemote.email == "mistry.mitul1@gmail.com") {
                Log.e(TAG, "Cannot demote the master admin account")
                return@withContext false
            }
            
            if (!userToDemote.isAdmin) {
                Log.d(TAG, "User ${userToDemote.email} is not an admin")
                return@withContext true
            }
            
            Log.d(TAG, "Master admin demoting ${userToDemote.email} from admin")
            val updatedUser = userToDemote.copy(isAdmin = false)
            val success = updateUserProfile(updatedUser)
            
            Log.d(TAG, "Admin demotion ${if (success) "succeeded" else "failed"}")
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error demoting user from admin: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun getUserRegistrationsData(userId: String): Pair<List<Event>, List<Hackathon>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching registrations for user $userId")
            
            val user = getUserById(userId)
            if (user == null) {
                Log.w(TAG, "getUserRegistrationsData: User not found: $userId")
                return@withContext Pair(emptyList(), emptyList())
            }
            
            // Get registered events - handle potential errors for each event
            val eventsList = mutableListOf<Event>()
            if (user.registeredEvents.isNotEmpty()) {
                try {
                    // Process in smaller batches to avoid issues with large lists
                    for (batch in user.registeredEvents.chunked(10)) {
                        try {
                            val batchEvents = getEventsByIds(batch)
                            eventsList.addAll(batchEvents)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching batch of events", e)
                            // Continue with next batch rather than failing completely
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing registered events", e)
                    // Continue with hackathons rather than failing completely
                }
            }
            
            // Get registered hackathons - handle potential errors for each hackathon
            val hackathonsList = mutableListOf<Hackathon>()
            if (user.registeredHackathons.isNotEmpty()) {
                try {
                    // Process in smaller batches to avoid issues with large lists
                    for (batch in user.registeredHackathons.chunked(10)) {
                        try {
                            val batchHackathons = getHackathonsByIds(batch)
                            hackathonsList.addAll(batchHackathons)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching batch of hackathons", e)
                            // Continue with next batch rather than failing completely
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing registered hackathons", e)
                }
            }
            
            Log.d(TAG, "Successfully loaded ${eventsList.size} events and ${hackathonsList.size} hackathons for user $userId")
            Pair(eventsList, hackathonsList)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user registrations: ${e.message}", e)
            Pair(emptyList(), emptyList())
        }
    }
    
    // Initialize a new user document with proper fields to avoid update issues
    suspend fun initNewUser(userId: String, email: String, name: String = "", photoUrl: String = ""): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing new user: $email")
            
            val isAdmin = email == "mistry.mitul1@gmail.com"
            
            val user = User(
                id = userId,
                uid = userId,
                email = email,
                displayName = name,
                name = name,
                photoUrl = photoUrl,
                profilePictureUrl = photoUrl,
                isAdmin = isAdmin,
                registeredEvents = emptyList(),
                registeredHackathons = emptyList()
            )
            
            // Check if user already exists first
            val existingUser = getUserById(userId)
            if (existingUser != null) {
                Log.d(TAG, "User already initialized, skipping")
                return@withContext true
            }
            
            // Create user document
            usersCollection.document(userId).set(user).await()
            
            Log.d(TAG, "User initialized successfully: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing user: ${e.message}", e)
            false
        }
    }

    // Registration details methods
    suspend fun saveRegistrationDetails(registrationDetails: RegistrationDetails): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving registration details for user ${registrationDetails.userId}")
            
            val registrationId = if (registrationDetails.id.isBlank()) {
                registrationsCollection.document().id
            } else {
                registrationDetails.id
            }
            
            val registrationWithId = registrationDetails.copy(id = registrationId)
            registrationsCollection.document(registrationId).set(registrationWithId).await()
            
            Log.d(TAG, "Successfully saved registration details with ID: $registrationId")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving registration details: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun getRegistrationDetailsByUserId(userId: String): List<RegistrationDetails> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching registration details for user $userId")
            val querySnapshot = registrationsCollection.whereEqualTo("userId", userId).get().await()
            val registrations = querySnapshot.toObjects(RegistrationDetails::class.java)
            Log.d(TAG, "Found ${registrations.size} registrations for user $userId")
            return@withContext registrations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching registration details for user $userId: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun getRegistrationDetailsForEvent(eventId: String): List<RegistrationDetails> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching registration details for event $eventId")
            val querySnapshot = registrationsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("isHackathon", false)
                .get().await()
            val registrations = querySnapshot.toObjects(RegistrationDetails::class.java)
            Log.d(TAG, "Found ${registrations.size} registrations for event $eventId")
            return@withContext registrations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching registration details for event $eventId: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun getRegistrationDetailsForHackathon(hackathonId: String): List<RegistrationDetails> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching registration details for hackathon $hackathonId")
            val querySnapshot = registrationsCollection
                .whereEqualTo("eventId", hackathonId)
                .whereEqualTo("isHackathon", true)
                .get().await()
            val registrations = querySnapshot.toObjects(RegistrationDetails::class.java)
            Log.d(TAG, "Found ${registrations.size} registrations for hackathon $hackathonId")
            return@withContext registrations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching registration details for hackathon $hackathonId: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    suspend fun getRegistrationDetailsById(registrationId: String): RegistrationDetails? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching registration details with ID $registrationId")
            val documentSnapshot = registrationsCollection.document(registrationId).get().await()
            return@withContext documentSnapshot.toObject(RegistrationDetails::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching registration details with ID $registrationId: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun updateRegistrationDetails(registrationDetails: RegistrationDetails): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating registration details with ID ${registrationDetails.id}")
            registrationsCollection.document(registrationDetails.id).set(registrationDetails, SetOptions.merge()).await()
            Log.d(TAG, "Successfully updated registration details")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating registration details: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun deleteRegistrationDetails(registrationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting registration details with ID $registrationId")
            registrationsCollection.document(registrationId).delete().await()
            Log.d(TAG, "Successfully deleted registration details")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting registration details: ${e.message}", e)
            return@withContext false
        }
    }

    companion object {
        private const val TAG = "FirebaseService"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_HACKATHONS = "hackathons"
        private const val COLLECTION_REGISTRATIONS = "registrations"
        private const val STORAGE_EVENTS = "events"

        private var instance: FirebaseService? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): FirebaseService {
            if (instance == null) {
                instance = FirebaseService()
            }
            return instance!!
        }
    }
} 