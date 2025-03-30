package com.mtl.My_Hack_X.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.User
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    // Use LaunchedEffect with a key that never changes
    val firebaseService = remember { FirebaseService.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    
    // Simplified state management
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    
    // State for registrations - initialize as empty
    var registeredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var registeredHackathons by remember { mutableStateOf<List<Hackathon>>(emptyList()) }
    var registrationsLoading by remember { mutableStateOf(false) }
    var registrationsError by remember { mutableStateOf<String?>(null) }
    
    // Add a new state variable for edit profile dialog
    var showEditProfileDialog by remember { mutableStateOf(false) }
    
    // Add these state variables for profile editing
    var editName by remember { mutableStateOf("") }
    var editPhotoUrl by remember { mutableStateOf("") }
    
    // Load user profile once when screen is created
    DisposableEffect(Unit) {
        isLoading = true
        
        coroutineScope.launch {
            try {
                // Basic authentication check
                val userId = firebaseService.getCurrentUserId()
                if (userId == null) {
                    Log.d("ProfileScreen", "User not logged in, navigating to login")
                    errorMessage = "Not logged in"
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                    return@launch
                }
                
                try {
                    // Get user profile
                    val userProfile = firebaseService.getCurrentUserProfile()
                    if (userProfile != null) {
                        user = userProfile
                    } else {
                        errorMessage = "Could not load profile"
                    }
                } catch (e: Exception) {
                    Log.e("ProfileScreen", "Error loading profile: ${e.message}", e)
                    errorMessage = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
                
                // Load registrations separately so profile still shows even if this fails
                if (user != null) {
                    registrationsLoading = true
                    try {
                        // Simple, direct data loading without retries
                        val result = firebaseService.getUserRegistrationsData(userId)
                        registeredEvents = result.first.filter { it.title.isNotBlank() }
                        registeredHackathons = result.second.filter { it.title.isNotBlank() }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error loading registrations: ${e.message}", e)
                        registrationsError = "Couldn't load your registrations"
                    } finally {
                        registrationsLoading = false
                    }
                }
            } catch (e: Exception) {
                // Catch-all for any unexpected errors
                Log.e("ProfileScreen", "Unexpected error: ${e.message}", e)
                errorMessage = "An unexpected error occurred"
                isLoading = false
            }
        }
        
        // Cleanup when leaving screen
        onDispose { }
    }
    
    // UI Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            firebaseService.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading UI
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Error UI
            else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.Home.route) }) {
                        Text("Return to Home")
                    }
                }
            }
            // Profile content
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    user?.let { currentUser ->
                        // Basic user info
                        item {
                            // Status message (if any)
                            statusMessage?.let {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { statusMessage = null }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // User header card with improved styling
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
    Column(
        modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                                    // Profile avatar placeholder
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = currentUser.name.firstOrNull()?.uppercase() ?: "U",
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
                                        text = currentUser.name.ifBlank { "User" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    
                Text(
                                        text = currentUser.email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    if (currentUser.isAdmin || currentUser.email == "mistry.mitul1@gmail.com") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Admin") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.AdminPanelSettings,
                                                    contentDescription = null,
                                                    Modifier.size(AssistChipDefaults.IconSize)
                                                )
                                            }
                                        )
                                    }
                                    
                                    // Add Edit Profile Button
        Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = {
                                            editName = currentUser.name
                                            editPhotoUrl = currentUser.profilePictureUrl ?: ""
                                            showEditProfileDialog = true
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Edit Profile")
                                    }
                                }
                            }
                            
                            // Admin Dashboard button - show directly for the master admin email
                            if (currentUser.isAdmin || currentUser.email == "mistry.mitul1@gmail.com") {
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.Admin.route)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Admin Dashboard")
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            
                            // Registrations header with improved styling
                            Text(
                                text = "Your Registrations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Registrations content
                        item {
                            if (registrationsLoading) {
                                Box(
            modifier = Modifier
                .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            } else if (registrationsError != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
        ) {
            Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Error, 
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                                            text = registrationsError ?: "Error loading your registrations",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Button(onClick = { navController.navigate(Screen.Home.route) }) {
                                            Text("Go to Home")
                                        }
                                    }
                                }
                            } else {
                                // Events section
                                SectionHeader(
                                    title = "Events (${registeredEvents.size})",
                                    icon = Icons.Default.Event
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                                // Events content
                                if (registeredEvents.isEmpty()) {
                                    EmptyStateCard(
                                        icon = Icons.Default.EventBusy,
                                        message = "You haven't registered for any events yet",
                                        buttonText = "Browse Events",
                                        onButtonClick = { navController.navigate(Screen.Events.route) }
                                    )
                                } else {
                                    SafeEventsList(registeredEvents, navController)
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Hackathons section
                                SectionHeader(
                                    title = "Hackathons (${registeredHackathons.size})",
                                    icon = Icons.Default.Code
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Hackathons content
                                if (registeredHackathons.isEmpty()) {
                                    EmptyStateCard(
                                        icon = Icons.Default.CodeOff,
                                        message = "You haven't registered for any hackathons yet",
                                        buttonText = "Browse Hackathons",
                                        onButtonClick = { navController.navigate(Screen.Hackathons.route) }
                                    )
                                } else {
                                    SafeHackathonsList(registeredHackathons, navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = editPhotoUrl,
                        onValueChange = { editPhotoUrl = it },
                        label = { Text("Profile Picture URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Text(
                        text = "Note: You can use any image URL. Profile changes will be applied immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                user?.let { currentUser ->
                                    val updatedUser = currentUser.copy(
                                        name = editName,
                                        displayName = editName,
                                        profilePictureUrl = editPhotoUrl,
                                        photoUrl = editPhotoUrl
                                    )
                                    
                                    val success = firebaseService.updateUserProfile(updatedUser)
                                    if (success) {
                                        // Update local state
                                        user = updatedUser
                                        statusMessage = "Profile updated successfully"
                                    } else {
                                        statusMessage = "Failed to update profile"
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error updating profile: ${e.message}", e)
                                statusMessage = "Error: ${e.message}"
                            } finally {
                                showEditProfileDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FilledTonalButton(
                onClick = onButtonClick
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun SafeEventsList(events: List<Event>, navController: NavController) {
    // No outer try-catch
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        items(events) { event ->
            // Create a key to force recomposition if there's an error
            key(event.id) {
                // Use state to track if an error occurred
                val errorState = remember { mutableStateOf(false) }
                val errorMessage = remember { mutableStateOf("") }
                
                // Use effect to safely try operations that might fail
                LaunchedEffect(event) {
                    try {
                        // Validate event data
                        if (event.title.isBlank() || event.startDate == null) {
                            errorState.value = true
                            errorMessage.value = "Invalid event data"
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error validating event: ${e.message}")
                        errorState.value = true
                        errorMessage.value = e.message ?: "Unknown error"
                    }
                }
                
                // Render based on error state
                if (errorState.value) {
            Card(
                modifier = Modifier
                            .width(260.dp)
                            .height(140.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Event data error")
                        }
                    }
                } else {
                    RegistrationCard(
                        title = event.title,
                        startDate = event.startDate ?: Date(),
                        imageUrl = event.imageUrl,
                        onClick = { 
                            navController.navigate("${Screen.EventDetails.route}/${event.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SafeHackathonsList(hackathons: List<Hackathon>, navController: NavController) {
    // No outer try-catch
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        items(hackathons) { hackathon ->
            // Create a key to force recomposition if there's an error
            key(hackathon.id) {
                // Use state to track if an error occurred
                val errorState = remember { mutableStateOf(false) }
                val errorMessage = remember { mutableStateOf("") }
                
                // Use effect to safely try operations that might fail
                LaunchedEffect(hackathon) {
                    try {
                        // Validate hackathon data
                        if (hackathon.title.isBlank() || hackathon.startDate == null) {
                            errorState.value = true
                            errorMessage.value = "Invalid hackathon data"
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error validating hackathon: ${e.message}")
                        errorState.value = true
                        errorMessage.value = e.message ?: "Unknown error"
                    }
                }
                
                // Render based on error state
                if (errorState.value) {
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .height(140.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hackathon data error")
                        }
                    }
                } else {
                    RegistrationCard(
                        title = hackathon.title,
                        startDate = hackathon.startDate ?: Date(),
                        imageUrl = hackathon.imageUrl,
                        onClick = { 
                            navController.navigate("${Screen.HackathonDetails.route}/${hackathon.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationCard(
    title: String,
    startDate: Date,
    imageUrl: String?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box {
            com.mtl.My_Hack_X.components.AsyncImage(
                imageUrl = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
            ) {}
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(startDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
} 