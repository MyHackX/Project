package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventDetailsScreen(navController: NavController, eventId: String) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    
    // State variables
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUserRegistered by remember { mutableStateOf(false) }
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showLoginSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var registrationInProgress by remember { mutableStateOf(false) }
    
    // Make a local copy of the event to avoid smart cast issues
    val eventData = event
    
    // Load event details and check user registration status
    LaunchedEffect(eventId) {
        isLoading = true
        errorMessage = null
        
        try {
            val eventDetails = firebaseService.getEventById(eventId)
            if (eventDetails != null) {
                event = eventDetails
                
                // Check if user is logged in and registered
                val currentUser = firebaseService.getCurrentUserProfile()
                isUserLoggedIn = currentUser != null
                
                if (currentUser != null) {
                    isUserRegistered = currentUser.registeredEvents.contains(eventId)
                }
            } else {
                errorMessage = "Event not found"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading event details: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // Register confirmation dialog
    if (showRegisterDialog && event != null) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text(text = if (isUserRegistered) "Cancel Registration" else "Register for Event") },
            text = {
                Text(
                    text = if (isUserRegistered)
                        "Are you sure you want to cancel your registration for ${event?.title}?"
                    else
                        "Are you sure you want to register for ${event?.title}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            registrationInProgress = true
                            val currentUserId = firebaseService.getCurrentUserId()
                            
                            if (currentUserId != null) {
                                val success = if (isUserRegistered) {
                                    firebaseService.unregisterUserFromEvent(currentUserId, eventId)
                                } else {
                                    firebaseService.registerUserForEvent(currentUserId, eventId)
                                }
                                
                                if (success) {
                                    // Update UI state
                                    isUserRegistered = !isUserRegistered
                                    snackbarMessage = if (isUserRegistered)
                                        "Successfully registered for the event!"
                                    else
                                        "Registration cancelled successfully."
                                    
                                    // Refresh event details to get updated participant count
                                    event = firebaseService.getEventById(eventId)
                                } else {
                                    snackbarMessage = "Failed to ${if (isUserRegistered) "cancel registration" else "register"}"
                                }
                                
                                showLoginSnackbar = true
                            }
                            
                            registrationInProgress = false
                            showRegisterDialog = false
                        }
                    },
                    enabled = !registrationInProgress
                ) {
                    Text(text = if (isUserRegistered) "Confirm Cancellation" else "Confirm Registration")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRegisterDialog = false },
                    enabled = !registrationInProgress
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }.apply {
                    if (showLoginSnackbar) {
                        coroutineScope.launch {
                            showSnackbar(snackbarMessage)
                            showLoginSnackbar = false
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
                modifier = Modifier
                    .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Go Back")
                    }
                }
            } else if (event != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Event image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(event?.imageUrl.takeIf { !it.isNullOrBlank() } 
                                    ?: "https://via.placeholder.com/800x400?text=Event+Image")
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Event image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Status chip
                Surface(
                            color = when (event?.status) {
                                EventStatus.ONGOING -> MaterialTheme.colorScheme.primary
                                EventStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                                EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                ) {
                    Text(
                                text = event?.status?.name ?: "UPCOMING",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Event details
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = event?.title ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Event type chip
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = event?.type?.name ?: "WORKSHOP",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Key event details in a card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Date and time
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Date",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Date & Time",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${dateFormat.format(event?.startDate ?: Date())} - ${dateFormat.format(event?.endDate ?: Date())}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (event?.startDate?.before(Date()) == true && event?.endDate?.after(Date()) == true) {
                                            Text(
                                                text = "Event is happening now!",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                // Location
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Location",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = event?.location ?: "No location specified",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                // Organizer
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Organizer",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Organized by",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = event?.organizerName ?: event?.organizer ?: "Unknown organizer",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                // Registration
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = "Registration",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Participants",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${event?.currentParticipants ?: 0} / ${event?.maxParticipants ?: "Unlimited"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        // Show if spaces available
                                        val localEvent = event
                                        if (localEvent != null && localEvent.maxParticipants != null && 
                                            localEvent.currentParticipants < localEvent.maxParticipants) {
                                            Text(
                                                text = "${localEvent.maxParticipants - localEvent.currentParticipants} spaces available",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else if (localEvent != null && localEvent.maxParticipants != null && 
                                            localEvent.currentParticipants >= localEvent.maxParticipants) {
                                            Text(
                                                text = "Fully booked!",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                
                                if (!event?.registrationUrl.isNullOrBlank()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    // External registration
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Registration Link",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "External Registration",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = event?.registrationUrl ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Description Section
                        Text(
                            text = "About This Event",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event?.description ?: "No description available.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Requirements Section
                        if (!event?.requirements.isNullOrBlank()) {
                            Text(
                                text = "Requirements",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = event?.requirements ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Tags Section
                        if (event?.tags?.isNotEmpty() == true) {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                event?.tags?.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Schedule timeline if available
                        if (!event?.description.isNullOrBlank()) {
                            Text(
                                text = "Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Example schedule items
                            ScheduleItem(
                                time = "Start: ${dateFormat.format(event?.startDate ?: Date())}",
                                title = "Event Begins",
                                description = "Welcome and introduction"
                            )
                            
                            ScheduleItem(
                                time = "End: ${dateFormat.format(event?.endDate ?: Date())}",
                                title = "Event Concludes",
                                description = "Closing remarks and networking"
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Divider()

                Spacer(modifier = Modifier.height(16.dp))

                        // Register/Unregister Button
                        if (event?.status == EventStatus.UPCOMING || event?.status == EventStatus.ONGOING) {
                Button(
                                onClick = {
                                    if (isUserLoggedIn) {
                                        // Navigate to registration form instead of showing dialog
                                        navController.navigate("registration/${event?.id}/false")
                                    } else {
                                        snackbarMessage = "Please login to register for events"
                                        showLoginSnackbar = true
                                    }
                                },
                    modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isUserRegistered) 
                                        MaterialTheme.colorScheme.error else 
                                        MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isUserRegistered) 
                                        Icons.Default.EventBusy else 
                                        Icons.Default.Event,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isUserRegistered) "Cancel Registration" else "Register for Event")
                            }
                            
                            if (!isUserLoggedIn) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.Login.route) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Login to Register")
                                }
                            }
                        } else if (event?.status == EventStatus.COMPLETED) {
                            // Show past event message
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("This event has already ended.")
                                }
                            }
                        } else if (event?.status == EventStatus.CANCELLED) {
                            // Show cancelled event message
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "This event has been cancelled.",
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            if (registrationInProgress) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ScheduleItem(time: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(72.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(12.dp)
            ) { }
            
            Divider(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Content
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
        )
        Text(
                text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
} 