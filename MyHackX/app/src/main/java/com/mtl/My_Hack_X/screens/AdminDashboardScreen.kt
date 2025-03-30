package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.User
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.screens.components.ErrorBoundary
import com.mtl.My_Hack_X.screens.components.EventCard
import com.mtl.My_Hack_X.screens.components.UserCard
import com.mtl.My_Hack_X.screens.dialogs.AddEventDialog
import com.mtl.My_Hack_X.screens.dialogs.AddUserDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.util.Log

private const val TAG = "AdminDashboardScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Log.d(TAG, "==== LIFECYCLE: AdminDashboardScreen composition started ====")
    
    // Use DisposableEffect to track screen lifecycle
    DisposableEffect(Unit) {
        Log.d(TAG, "==== LIFECYCLE: AdminDashboardScreen entered ====")
        onDispose {
            Log.d(TAG, "==== LIFECYCLE: AdminDashboardScreen disposed ====")
        }
    }
    
    ErrorBoundary {
        AdminDashboardContent(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardContent(navController: NavController) {
    // Add lifecycle tracking
    Log.d(TAG, "==== LIFECYCLE: AdminDashboardContent started ====")
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var hasRenderError by remember { mutableStateOf(false) }
    var renderErrorMessage by remember { mutableStateOf("") }
    var hasNavigatedAway by remember { mutableStateOf(false) }
    var loadAttempts by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val firebaseService = remember { FirebaseService.getInstance() }

    // Crash protection - handle UI exceptions
    var hasError by remember { mutableStateOf(false) }
    
    // Check admin rights - with retry logic and delay
    LaunchedEffect(loadAttempts) {
        if (hasNavigatedAway) return@LaunchedEffect
        
        Log.d(TAG, "==== LIFECYCLE: Admin check attempt $loadAttempts ====")
        isLoading = true
        
        try {
            delay(500) // Delay to ensure Firebase is ready
            val currentUser = firebaseService.getCurrentUserProfile()
            Log.d(TAG, "Current user: ${currentUser?.email}, admin: ${currentUser?.isAdmin}")
            
            if (currentUser == null) {
                Log.d(TAG, "Current user is null, will retry if attempts < 3")
                if (loadAttempts < 3) {
                    loadAttempts++
                    return@LaunchedEffect
                }
            }
            
            // Always grant admin to this email or if user has admin flag
            isAdmin = currentUser?.isAdmin == true || currentUser?.email == "mistry.mitul1@gmail.com"
            
            if (!isAdmin) {
                Log.d(TAG, "User is not an admin, showing error message")
                errorMessage = "Admin access required"
                // Don't navigate away immediately - show error message
                
                // Set a delayed navigation
                scope.launch {
                    delay(3000) // 3 seconds to read the error
                    if (!hasNavigatedAway) {
                        Log.d(TAG, "Navigating back due to lack of admin rights")
                        hasNavigatedAway = true
                        navController.navigateUp()
                    }
                }
            } else {
                Log.d(TAG, "User has admin access, proceeding to load data")
                // Will load data in the second LaunchedEffect
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status: ${e.message}", e)
            errorMessage = "Error verifying admin status: ${e.message ?: "Unknown error"}"
            hasError = true
            
            // If we've tried too many times, stop retrying
            if (loadAttempts < 3) {
                loadAttempts++
            }
        } finally {
            isLoading = false
        }
    }

    // Initial data loading
    LaunchedEffect(selectedTab) {
        if (hasError || !isAdmin || hasNavigatedAway) return@LaunchedEffect
        
        Log.d(TAG, "==== LIFECYCLE: Loading data for tab $selectedTab ====")
        
        isLoading = true
        errorMessage = null
        
        try {
            when (selectedTab) {
                0 -> {
                    // Load events
                    Log.d(TAG, "Loading events")
                    try {
                        val loadedEvents = firebaseService.getAllEvents()
                        if (loadedEvents != null) {
                            events = loadedEvents
                            Log.d(TAG, "Successfully loaded ${events.size} events")
                        } else {
                            Log.w(TAG, "Events list was null")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading events: ${e.message}", e)
                        errorMessage = "Failed to load events: ${e.message ?: "Unknown error"}"
                    }
                }
                1 -> {
                    // Load users
                    Log.d(TAG, "Loading users")
                    try {
                        val loadedUsers = firebaseService.getAllUsers()
                        if (loadedUsers != null) {
                            users = loadedUsers
                            Log.d(TAG, "Successfully loaded ${users.size} users")
                        } else {
                            Log.w(TAG, "Users list was null")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading users: ${e.message}", e)
                        errorMessage = "Failed to load users: ${e.message ?: "Unknown error"}"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "General error in data loading: ${e.message}", e)
            errorMessage = "Error loading data: ${e.message ?: "Unknown error"}"
            hasError = true
        } finally {
            isLoading = false
            Log.d(TAG, "Finished loading data for tab $selectedTab")
        }
    }

    // Use error handler pattern that's compatible with Compose
    if (hasRenderError) {
        // Minimal error UI as fallback
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "An error occurred in the Admin Dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                renderErrorMessage,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    if (!hasNavigatedAway) {
                        hasNavigatedAway = true
                        navController.navigateUp() 
                    }
                }
            ) {
                Text("Go Back")
            }
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Admin Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { 
                            Log.d(TAG, "Back button clicked")
                            navController.navigateUp() 
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            Log.d(TAG, "Add button clicked for tab: $selectedTab")
                            when (selectedTab) {
                                0 -> showAddEventDialog = true
                                1 -> showAddUserDialog = true
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add new item")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Error message
                errorMessage?.let { error ->
                    Log.d(TAG, "Displaying error: $error")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { 
                            Log.d(TAG, "Switching to Events tab") 
                            selectedTab = 0 
                        },
                        text = { Text("Events") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { 
                            Log.d(TAG, "Switching to Users tab")
                            selectedTab = 1 
                        },
                        text = { Text("Users") }
                    )
                }

                when (selectedTab) {
                    0 -> EventsManagementTab(
                        events = events,
                        isLoading = isLoading,
                        onEditEvent = { 
                            Log.d(TAG, "Edit event: ${it.id}") 
                            // Handle edit
                        },
                        onDeleteEvent = { eventId ->
                            Log.d(TAG, "Delete event: $eventId")
                            scope.launch {
                                try {
                                    val success = firebaseService.deleteEvent(eventId)
                                    if (success) {
                                        // Refresh events list
                                        Log.d(TAG, "Event deleted successfully, refreshing list")
                                        events = firebaseService.getAllEvents() ?: emptyList()
                                        successMessage = "Event deleted successfully"
                                        showSuccessDialog = true
                                        
                                        // Auto dismiss after delay
                                        scope.launch {
                                            delay(2500)
                                            showSuccessDialog = false
                                        }
                                    } else {
                                        Log.e(TAG, "Failed to delete event")
                                        errorMessage = "Failed to delete event"
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error deleting event: ${e.message}", e)
                                    errorMessage = "Error: ${e.message ?: "Unknown error"}"
                                }
                            }
                        }
                    )
                    1 -> UsersManagementTab(
                        users = users,
                        isLoading = isLoading,
                        onEditUser = { 
                            Log.d(TAG, "Edit user: ${it.id}")
                            // Handle edit
                        },
                        onDeleteUser = { userId ->
                            Log.d(TAG, "Delete user: $userId")
                            scope.launch {
                                try {
                                    // This method doesn't exist in our service, create a placeholder that just returns false
                                    // val success = firebaseService.deleteUser(userId)
                                    errorMessage = "Delete user functionality not implemented"
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error handling user action: ${e.message}", e)
                                    errorMessage = "Error: ${e.message ?: "Unknown error"}"
                                }
                            }
                        }
                    )
                }
            }

            if (showAddEventDialog) {
                AddEventDialog(
                    onDismiss = { 
                        Log.d(TAG, "Dismissing event dialog")
                        showAddEventDialog = false 
                    },
                    onAddEvent = { event ->
                        Log.d(TAG, "Adding new event: ${event.title}")
                        scope.launch {
                            try {
                                val createdEvent = firebaseService.createEvent(event)
                                if (createdEvent.id.isNotEmpty()) {
                                    showAddEventDialog = false
                                    // Show success dialog
                                    successMessage = "Event \"${event.title}\" created successfully!"
                                    showSuccessDialog = true
                                    
                                    // Auto dismiss after delay
                                    scope.launch {
                                        delay(2500)
                                        showSuccessDialog = false
                                    }
                                    
                                    // Refresh events list
                                    Log.d(TAG, "Refreshing events list after adding new event")
                                    events = firebaseService.getAllEvents() ?: emptyList()
                                } else {
                                    Log.e(TAG, "Failed to create event - empty ID returned")
                                    errorMessage = "Failed to create event"
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error creating event: ${e.message}", e)
                                errorMessage = "Error: ${e.message ?: "Unknown error"}"
                            }
                        }
                    }
                )
            }

            if (showAddUserDialog) {
                AddUserDialog(
                    onDismiss = { 
                        Log.d(TAG, "Dismissing user dialog")
                        showAddUserDialog = false 
                    },
                    onAddUser = { user ->
                        Log.d(TAG, "Adding new user: ${user.email}")
                        scope.launch {
                            try {
                                val success = firebaseService.createUserProfile(user)
                                if (success) {
                                    showAddUserDialog = false
                                    // Show success dialog
                                    successMessage = "User \"${user.name}\" created successfully!"
                                    showSuccessDialog = true
                                    
                                    // Auto dismiss after delay
                                    scope.launch {
                                        delay(2500)
                                        showSuccessDialog = false
                                    }
                                    
                                    // Refresh users list
                                    Log.d(TAG, "Refreshing users list after adding new user")
                                    users = firebaseService.getAllUsers() ?: emptyList()
                                } else {
                                    Log.e(TAG, "Failed to create user")
                                    errorMessage = "Failed to create user"
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error creating user: ${e.message}", e)
                                errorMessage = "Error: ${e.message ?: "Unknown error"}"
                            }
                        }
                    }
                )
            }
            
            // Success Dialog with animation
            if (showSuccessDialog) {
                SuccessDialog(
                    message = successMessage,
                    onDismiss = { 
                        Log.d(TAG, "Dismissing success dialog")
                        showSuccessDialog = false 
                    }
                )
            }
        }
    }
}

@Composable
private fun EventsManagementTab(
    events: List<Event>,
    isLoading: Boolean,
    onEditEvent: (Event) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No events found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onEdit = { onEditEvent(event) },
                    onDelete = { onDeleteEvent(event.id) }
                )
            }
        }
    }
}

@Composable
private fun UsersManagementTab(
    users: List<User>,
    isLoading: Boolean,
    onEditUser: (User) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(users) { user ->
                UserCard(
                    user = user,
                    onEdit = { onEditUser(user) },
                    onDelete = { onDeleteUser(user.id) }
                )
            }
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
} 