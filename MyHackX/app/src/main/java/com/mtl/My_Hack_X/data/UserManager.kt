package com.mtl.My_Hack_X.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateListOf
import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.mtl.My_Hack_X.data.model.User
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

object UserManager {
    private val scope = MainScope()
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private lateinit var databaseHelper: DatabaseHelper
    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private var isInitialized = false

    fun init(dbHelper: DatabaseHelper) {
        if (!isInitialized) {
            databaseHelper = dbHelper
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
                // Load user data when auth state changes
                scope.launch {
                    loadUser()
                }
            }
            isInitialized = true
        }
    }

    private suspend fun loadUser() {
        val userId = databaseHelper.getString("user_id")
        if (userId.isNotEmpty()) {
            val user = databaseHelper.getFirebaseRepository().getUserById(userId)
            if (user != null) {
                setCurrentUser(user)
            } else {
                clearUser()
            }
        }
    }

    fun isAdmin(email: String): Boolean {
        // TODO: Implement proper admin check from Firebase
        return email == ADMIN_EMAIL
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
        clearUser()
    }

    private val _users = mutableStateListOf<User>()
    val users = _users

    private const val ADMIN_EMAIL = "mtlmistry123@gmail.com"
    private const val ADMIN_PASSWORD = "12345678"

    suspend fun registerUser(name: String, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        displayName = name,
                        email = email,
                        isAdmin = email == ADMIN_EMAIL
                    )
                    databaseHelper.getFirebaseRepository().createUser(user)
                    setCurrentUser(user)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val user = databaseHelper.getFirebaseRepository().getUserById(firebaseUser.uid)
                    if (user != null) {
                        setCurrentUser(user)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUserFlow.value = null
    }

    suspend fun getUserRegistrations(): List<HackathonEvent> {
        return withContext(Dispatchers.IO) {
            _currentUserFlow.value?.let { user ->
                databaseHelper.getFirebaseRepository().getUserRegistrations(user.uid)
            } ?: emptyList()
        }
    }

    suspend fun registerForEvent(userId: String, eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databaseHelper.getFirebaseRepository().registerForHackathon(eventId, userId)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun setCurrentUser(user: User) {
        _currentUserFlow.value = user
        databaseHelper.saveString("user_id", user.uid)
    }

    fun clearUser() {
        _currentUserFlow.value = null
        databaseHelper.saveString("user_id", "")
    }

    fun isUserLoggedIn(): Boolean {
        return _currentUserFlow.value != null
    }

    fun isAdmin(): Boolean {
        return _currentUserFlow.value?.isAdmin ?: false
    }
} 