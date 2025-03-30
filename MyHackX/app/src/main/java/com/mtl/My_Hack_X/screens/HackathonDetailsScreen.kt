package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.mtl.My_Hack_X.components.AsyncImage
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonDetailsScreen(navController: NavController, hackathonId: String) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val scope = rememberCoroutineScope()
    
    // State variables
    var hackathon by remember { mutableStateOf<Hackathon?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isRegistered by remember { mutableStateOf(false) }
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var registrationMessage by remember { mutableStateOf<String?>(null) }
    
    // Load hackathon and check registration status
    LaunchedEffect(hackathonId) {
        isLoading = true
        error = null
        
        try {
            // Check if user is logged in
            val userId = firebaseService.getCurrentUserId()
            isUserLoggedIn = userId != null
            
            // Get hackathon details
            val hackathonResult = firebaseService.getHackathonById(hackathonId)
            if (hackathonResult != null) {
                hackathon = hackathonResult
                
                // Check if user is registered for this hackathon
                if (isUserLoggedIn && userId != null) {
                    isRegistered = hackathonResult.registeredUsers.contains(userId)
                }
                
                isLoading = false
            } else {
                error = "Failed to load hackathon details: Hackathon not found"
                isLoading = false
            }
        } catch (e: Exception) {
            error = "Failed to load hackathon details: ${e.message}"
            isLoading = false
        }
    }
    
    // Handle registration/unregistration
    fun registerForHackathon() {
        val userId = firebaseService.getCurrentUserId() ?: return
        
        scope.launch {
            try {
                val success = firebaseService.registerUserForHackathon(userId, hackathonId)
                if (success) {
                    isRegistered = true
                    registrationMessage = "Successfully registered for the hackathon!"
                    // Refresh hackathon details
                    hackathon = firebaseService.getHackathonById(hackathonId)
                } else {
                    registrationMessage = "Failed to register: Unknown error"
                }
            } catch (e: Exception) {
                registrationMessage = "Failed to register: ${e.message}"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hackathon?.title ?: "Hackathon Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hackathon?.organizer == firebaseService.getCurrentUserId()) {
                        IconButton(onClick = { /* Navigate to edit screen */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = error ?: "An unknown error occurred",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                hackathon != null -> {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Header section with image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            val context = LocalContext.current
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(hackathon?.imageUrl.takeIf { !it.isNullOrBlank() } 
                                        ?: "https://via.placeholder.com/800x400?text=Hackathon+Image")
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = "Hackathon image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Add status chip on top of the image
                            Surface(
                                color = when (hackathon!!.status) {
                                    HackathonStatus.ONGOING -> MaterialTheme.colorScheme.primary
                                    HackathonStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                                    HackathonStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.tertiary
                                },
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = hackathon!!.status.name,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Title and status
                        Text(
                            text = hackathon!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Date and location
                        HackathonInfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Dates",
                            value = "${formatDate(hackathon!!.startDate)} - ${formatDate(hackathon!!.endDate)}"
                        )
                        
                        HackathonInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = hackathon!!.location
                        )
                        
                        HackathonInfoRow(
                            icon = Icons.Default.EmojiEvents,
                            label = "Prize Pool",
                            value = hackathon!!.prizePool
                        )
                        
                        HackathonInfoRow(
                            icon = Icons.Default.Group,
                            label = "Teams",
                            value = "${hackathon!!.teams}/${hackathon!!.teamSize}"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Description
                        Text(
                            text = "About This Hackathon",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = hackathon!!.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Prize Breakdown
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Prize Details",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Prize cards
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total Prize Pool",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = hackathon!!.prizePool,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PrizeCard(position = "1st Place", prize = "60% of prize pool")
                                    PrizeCard(position = "2nd Place", prize = "30% of prize pool")
                                    PrizeCard(position = "3rd Place", prize = "10% of prize pool")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Team Information
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Team Information",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Team Size",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${hackathon!!.teamSize} members per team",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Divider(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .width(1.dp)
                                    )
                                    
                                    Column {
                                        Text(
                                            text = "Max Teams",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${hackathon!!.teams} teams",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Divider(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .width(1.dp)
                                    )
                                    
                                    Column {
                                        Text(
                                            text = "Participants",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = "${hackathon!!.currentParticipants}/${hackathon!!.maxParticipants}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                if (hackathon!!.currentParticipants < hackathon!!.maxParticipants) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${hackathon!!.maxParticipants - hackathon!!.currentParticipants} spots remaining!",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Themes and Track
                        if (hackathon!!.themes.isNotEmpty()) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Tracks & Themes",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(hackathon!!.themes) { theme ->
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = theme,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Requirements section
                        if (hackathon!!.requirements.isNotEmpty()) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Requirements",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = hackathon!!.requirements,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Sponsors section
                        if (hackathon!!.sponsors.isNotEmpty()) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Sponsors",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(hackathon!!.sponsors) { sponsor ->
                                    SponsorCard(name = sponsor)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Judges section
                        if (hackathon!!.judges.isNotEmpty()) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Judges",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(hackathon!!.judges) { judge ->
                                    JudgeCard(name = judge)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Timeline/Schedule
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Event Schedule",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Day 1
                        TimelineEvent(
                            time = formatDate(hackathon!!.startDate) + " (Day 1)",
                            events = listOf(
                                "9:00 AM - Registration & Check-in",
                                "10:00 AM - Opening Ceremony",
                                "11:00 AM - Team Formation",
                                "12:00 PM - Hacking Begins",
                                "7:00 PM - Dinner",
                                "11:59 PM - Day 1 Checkpoints"
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Day 2
                        TimelineEvent(
                            time = formatDate(hackathon!!.endDate) + " (Day 2)",
                            events = listOf(
                                "9:00 AM - Breakfast",
                                "12:00 PM - Project Submission Deadline",
                                "1:00 PM - Presentations Begin",
                                "3:00 PM - Judging",
                                "4:30 PM - Awards Ceremony",
                                "5:30 PM - Closing Remarks & Networking"
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // External Registration Link
                        if (!hackathon!!.registrationUrl.isBlank()) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "External Registration",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "This hackathon uses an external registration system.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    OutlinedButton(
                                        onClick = { /* Open registration URL */ }
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Visit Registration Page")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Registration Button
                        if (hackathon!!.status == HackathonStatus.UPCOMING || hackathon!!.status == HackathonStatus.ONGOING) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    if (isUserLoggedIn) {
                                        // Navigate to registration form instead of showing confirmation dialog
                                        navController.navigate("registration/${hackathon?.id}/true")
                                    } else {
                                        navController.navigate(Screen.Login.route)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRegistered) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isRegistered) 
                                        Icons.Default.Close 
                                    else 
                                        Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRegistered) 
                                        "Cancel Registration" 
                                    else 
                                        "Register for Hackathon"
                                )
                            }
                            
                            if (!isUserLoggedIn) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You need to be logged in to register",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else if (hackathon!!.status == HackathonStatus.COMPLETED) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
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
                                    Text("This hackathon has already concluded.")
                                }
                            }
                        } else if (hackathon!!.status == HackathonStatus.CANCELLED) {
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
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
                                        "This hackathon has been cancelled.",
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        
        if (showConfirmationDialog) {
            RegistrationConfirmationDialog(
                hackathon = hackathon!!,
                isRegistered = isRegistered,
                onConfirm = {
                    registerForHackathon()
                    showConfirmationDialog = false
                },
                onDismiss = {
                    showConfirmationDialog = false
                }
            )
        }
        
        if (registrationMessage != null) {
            LaunchedEffect(registrationMessage) {
                // Auto-dismiss after 3 seconds
                delay(3000)
                registrationMessage = null
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { registrationMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(registrationMessage!!)
                }
            }
        }
    }
}

@Composable
fun HackathonInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PrizeCard(position: String, prize: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = position,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = prize,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimelineEvent(time: String, events: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Timeline with dots
        events.forEach { event ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(8.dp)
                        .padding(top = 6.dp)
                ) { }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = event,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SponsorCard(name: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun JudgeCard(name: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RegistrationConfirmationDialog(
    hackathon: Hackathon,
    isRegistered: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRegistered) 
                    "Cancel Registration" 
                else 
                    "Confirm Registration"
            )
        },
        text = {
            Column {
                Text(
                    text = if (isRegistered)
                        "Are you sure you want to cancel your registration for this hackathon?"
                    else
                        "Are you sure you want to register for this hackathon?"
                )
                
                if (!isRegistered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Hackathon: ${hackathon.title}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Date: ${formatDate(hackathon.startDate)} - ${formatDate(hackathon.endDate)}")
                    Text(text = "Location: ${hackathon.location}")
                    Text(text = "Team Size: ${hackathon.teamSize} members per team")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegistered) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isRegistered) 
                        "Confirm Cancellation" 
                    else 
                        "Confirm Registration"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(date)
} 