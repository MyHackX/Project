package com.mtl.My_Hack_X.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtl.My_Hack_X.viewmodels.AdminViewModel
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Date
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextOverflow
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.*
import com.mtl.My_Hack_X.components.DatePicker
import com.mtl.My_Hack_X.components.DatePickerButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val viewModel: AdminViewModel = viewModel()
    val firebaseService = remember { FirebaseService.getInstance() }
    val scope = rememberCoroutineScope()
    
    // Get the user's login status and admin status
    val currentUser = remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(Unit) {
        currentUser.value = firebaseService.getCurrentUserProfile()
        if (currentUser.value == null || !currentUser.value!!.isAdmin) {
            navController.navigateUp()
        }
    }
    
    // Use the state variables from viewModel rather than creating new ones
    var isLoadingState by remember { mutableStateOf(false) }
    val isLoadingFlow by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Separate message display state from the viewModel message
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }
    
    // State for current user
    var isAdmin by remember { mutableStateOf(false) }
    var hasAdminAccess by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }
    
    // Check admin status
    LaunchedEffect(Unit) {
        isLoadingState = true
        try {
            val user = firebaseService.getCurrentUserProfile()
            if (user != null) {
                isAdmin = user.isAdmin
                userEmail = user.email
                
                // Grant admin access to mistry.mitul1@gmail.com regardless of isAdmin flag
                hasAdminAccess = isAdmin || userEmail == "mistry.mitul1@gmail.com"
                
                // If user has the special email but not admin privileges, auto-promote them
                if (userEmail == "mistry.mitul1@gmail.com" && !isAdmin) {
                    scope.launch {
                        val updatedUser = user.copy(isAdmin = true)
                        firebaseService.updateUserProfile(updatedUser)
                        isAdmin = true
                    }
                }
            } else {
                hasAdminAccess = false
            }
            
            // Load data for admin panel
            scope.launch {
                viewModel.loadEvents()
                viewModel.loadHackathons()
                viewModel.loadUsers()
            }
        } catch (e: Exception) {
            hasAdminAccess = false
        } finally {
            isLoadingState = false
        }
    }
    
    // Display non-admin message instead of redirecting
    if (!isLoadingState && !hasAdminAccess) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Admin Access Required",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You don't have admin privileges to access this page.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Go Back")
            }
            
            return
        }
    }

    // Collect states
    val events by viewModel.events.collectAsState()
    val hackathons by viewModel.hackathons.collectAsState()
    val users by viewModel.users.collectAsState()
    
    // UI State
    var showEventDialog by remember { mutableStateOf(false) }
    var showHackathonDialog by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    // Add state variables for admin confirmation dialogs
    var showPromoteAdminConfirmation by remember { mutableStateOf(false) }
    var showRemoveAdminConfirmation by remember { mutableStateOf(false) }
    var userToPromote by remember { mutableStateOf<User?>(null) }
    var userToDemote by remember { mutableStateOf<User?>(null) }

    // Collect error messages from viewModel
    val errorMsg by viewModel.errorMessage.collectAsState()
    
    // Show error message if any
    if (errorMsg != null) {
        LaunchedEffect(errorMsg) {
            message = errorMsg ?: "An error occurred"
            showMessage = true
            delay(3000L)
            viewModel.clearErrorMessage()
        }
    }

    // Add state variables for edit dialogs and selected items
    var showEditEventDialog by remember { mutableStateOf(false) }
    var showEditHackathonDialog by remember { mutableStateOf(false) }
    var selectedEventForEdit by remember { mutableStateOf<Event?>(null) }
    var selectedHackathonForEdit by remember { mutableStateOf<Hackathon?>(null) }

    // Add this near the top with the other state variables
    var selectedUserForView by remember { mutableStateOf<User?>(null) }
    var showUserDetailsDialog by remember { mutableStateOf(false) }

    // Add state variables for edit dialogs at the top of the AdminScreen composable:
    var editEventId by remember { mutableStateOf("") }
    var editHackathonId by remember { mutableStateOf("") }

    // Event dialog state variables
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventImageUrl by remember { mutableStateOf("") }

    // Hackathon dialog state variables
    var hackathonTitle by remember { mutableStateOf("") }
    var hackathonDescription by remember { mutableStateOf("") }
    var hackathonDate by remember { mutableStateOf("") }
    var hackathonLocation by remember { mutableStateOf("") }
    var hackathonImageUrl by remember { mutableStateOf("") }

    if (isLoadingState) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
    Scaffold(
        topBar = {
            TopAppBar(
                    title = { Text("Admin Dashboard") }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.AddEvent.route) },
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text("Create Event")
                        }
                        
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.AddHackathon.route) },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("Create Hackathon")
                        }
                        
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.AdminDashboard.route) },
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Text("Advanced")
                        }
                    }
                }
                
                // Events section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Events",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            if (viewModel.events.value.isEmpty()) {
                                Text(
                                    text = "No events found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Events list with LazyColumn inside this item
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 300.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(events) { event ->
                                        EventCard(event, navController, viewModel, scope)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Hackathons section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Hackathons",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            if (viewModel.hackathons.value.isEmpty()) {
                                Text(
                                    text = "No hackathons found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Hackathons list with LazyColumn inside this item
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 300.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(hackathons) { hackathon ->
                                        HackathonCard(hackathon, navController, viewModel, scope)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Users section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Users",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            if (viewModel.users.value.isEmpty()) {
                                Text(
                                    text = "No users found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Users list
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 300.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(users) { user ->
                                        UserCard(user, navController, viewModel, scope)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Show message snackbar
            if (showSnackbar && snackbarMessage != null) {
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(snackbarMessage) {
                    snackbarHostState.showSnackbar(message = snackbarMessage!!)
                    showSnackbar = false
                }
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }

    // Create Event Dialog
    if (showEventDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf(Date()) }
        var endDate by remember { mutableStateOf(Date()) }
        var location by remember { mutableStateOf("") }
        var maxParticipantsText by remember { mutableStateOf("") }
        var organizer by remember { mutableStateOf("") }
        var imageUrl by remember { mutableStateOf("") }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = { Text("Create New Event") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Event Description") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var eventDateObj by remember { 
                        mutableStateOf<Date?>(
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(eventDate)
                            } catch (e: Exception) {
                                Date()
                            }
                        )
                    }
                    
                    // Effect to update string date when date object changes
                    LaunchedEffect(eventDateObj) {
                        eventDateObj?.let {
                            eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        }
                    }
                    
                    DatePicker(
                        selectedDate = eventDateObj,
                        onDateSelected = { eventDateObj = it },
                        label = "Event Date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxParticipantsText,
                        onValueChange = { maxParticipantsText = it },
                        label = { Text("Max Participants") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = organizer,
                        onValueChange = { organizer = it },
                        label = { Text("Organizer") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEventDialog = false
                        viewModel.createEvent(
                            title = title,
                            description = description,
                            startDate = startDate,
                            endDate = endDate,
                            location = location,
                            maxParticipants = maxParticipantsText.toIntOrNull() ?: 50,
                            organizer = organizer,
                            imageUrl = imageUrl
                        )
                        snackbarMessage = "Event created successfully"
                        showSnackbar = true
                        
                        // Force immediate reload with multiple attempts
                        scope.launch {
                            var attempt = 0
                            while (attempt < 3) {
                                val delayTime = 500L * (attempt + 1)
                                delay(delayTime)
                                Log.d("AdminScreen", "Reloading events attempt ${attempt + 1}")
                                viewModel.loadEvents()
                                attempt++
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Hackathon Dialog
    if (showHackathonDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var startDate by remember { mutableStateOf(Date()) }
        var endDate by remember { mutableStateOf(Date()) }
        var location by remember { mutableStateOf("") }
        var maxParticipantsText by remember { mutableStateOf("") }
        var prizePool by remember { mutableStateOf("") }
        var organizer by remember { mutableStateOf("") }
        var imageUrl by remember { mutableStateOf("") }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        AlertDialog(
            onDismissRequest = { showHackathonDialog = false },
            title = { Text("Create New Hackathon") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Hackathon Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Hackathon Description") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var hackathonDateObj by remember { 
                        mutableStateOf<Date?>(
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(hackathonDate)
                            } catch (e: Exception) {
                                Date()
                            }
                        )
                    }
                    
                    // Effect to update string date when date object changes
                    LaunchedEffect(hackathonDateObj) {
                        hackathonDateObj?.let {
                            hackathonDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        }
                    }
                    
                    DatePicker(
                        selectedDate = hackathonDateObj,
                        onDateSelected = { hackathonDateObj = it },
                        label = "Hackathon Date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxParticipantsText,
                        onValueChange = { maxParticipantsText = it },
                        label = { Text("Max Participants") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prizePool,
                        onValueChange = { prizePool = it },
                        label = { Text("Prize Pool") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = organizer,
                        onValueChange = { organizer = it },
                        label = { Text("Organizer") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHackathonDialog = false
                        viewModel.createHackathon(
                            title = title,
                            description = description,
                            startDate = startDate,
                            endDate = endDate,
                            location = location,
                            maxParticipants = maxParticipantsText.toIntOrNull() ?: 50,
                            prizePool = prizePool,
                            organizer = organizer,
                            imageUrl = imageUrl
                        )
                        snackbarMessage = "Hackathon created successfully"
                        showSnackbar = true
                        
                        // Force immediate reload with multiple attempts
                        scope.launch {
                            var attempt = 0
                            while (attempt < 3) {
                                val delayTime = 500L * (attempt + 1)
                                delay(delayTime)
                                Log.d("AdminScreen", "Reloading hackathons attempt ${attempt + 1}")
                                viewModel.loadHackathons()
                                attempt++
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHackathonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit Event Dialog
    if (showEditEventDialog) {
        AlertDialog(
            onDismissRequest = { showEditEventDialog = false },
            title = { Text("Edit Event") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = eventDescription,
                        onValueChange = { eventDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var eventDateObj by remember { 
                        mutableStateOf<Date?>(
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(eventDate)
                            } catch (e: Exception) {
                                Date()
                            }
                        )
                    }
                    
                    // Effect to update string date when date object changes
                    LaunchedEffect(eventDateObj) {
                        eventDateObj?.let {
                            eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        }
                    }
                    
                    DatePicker(
                        selectedDate = eventDateObj,
                        onDateSelected = { eventDateObj = it },
                        label = "Event Date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { eventLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = eventImageUrl,
                        onValueChange = { eventImageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val parsedDate = try {
                                dateFormat.parse(eventDate)
                            } catch (e: Exception) {
                                Date() // Use current date if parsing fails
                            }
                            
                            val updatedEvent = Event(
                                id = editEventId,
                                title = eventTitle,
                                description = eventDescription,
                                startDate = parsedDate,
                                location = eventLocation,
                                imageUrl = eventImageUrl,
                                organizerName = "Admin", // Default value
                                registrationUrl = "" // Default value
                            )
                            
                            viewModel.updateEvent(updatedEvent)
                            showEditEventDialog = false
                            
                            // Refresh events list after edit
                            delay(500) // Wait for Firestore update
                            viewModel.loadEvents()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEventDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Hackathon Dialog
    if (showEditHackathonDialog) {
        AlertDialog(
            onDismissRequest = { showEditHackathonDialog = false },
            title = { Text("Edit Hackathon") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = hackathonTitle,
                        onValueChange = { hackathonTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = hackathonDescription,
                        onValueChange = { hackathonDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var hackathonDateObj by remember { 
                        mutableStateOf<Date?>(
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(hackathonDate)
                            } catch (e: Exception) {
                                Date()
                            }
                        )
                    }
                    
                    // Effect to update string date when date object changes
                    LaunchedEffect(hackathonDateObj) {
                        hackathonDateObj?.let {
                            hackathonDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        }
                    }
                    
                    DatePicker(
                        selectedDate = hackathonDateObj,
                        onDateSelected = { hackathonDateObj = it },
                        label = "Hackathon Date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = hackathonLocation,
                        onValueChange = { hackathonLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = hackathonImageUrl,
                        onValueChange = { hackathonImageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val parsedDate = try {
                                dateFormat.parse(hackathonDate)
                            } catch (e: Exception) {
                                Date() // Use current date if parsing fails
                            }
                            
                            val updatedHackathon = Hackathon(
                                id = editHackathonId,
                                title = hackathonTitle,
                                description = hackathonDescription,
                                startDate = parsedDate,
                                location = hackathonLocation,
                                imageUrl = hackathonImageUrl,
                                organizerName = "Admin", // Default value
                                registrationUrl = "" // Default value
                            )
                            
                            viewModel.updateHackathon(updatedHackathon)
                            showEditHackathonDialog = false
                            
                            // Refresh hackathons list after edit
                            delay(500) // Wait for Firestore update
                            viewModel.loadHackathons()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditHackathonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // User Details Dialog
    if (showUserDetailsDialog && selectedUserForView != null) {
        val user = selectedUserForView!!
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        AlertDialog(
            onDismissRequest = { 
                showUserDetailsDialog = false
                selectedUserForView = null
            },
            title = { Text("User Profile: ${user.displayName}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Basic profile info
                    Text("User ID: ${user.id}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Admin Status: ${if (user.isAdmin) "Admin" else "Regular User"}", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (user.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Registered Events section
                    Text("Registered Events (${user.registeredEvents.size})", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (user.registeredEvents.isEmpty()) {
                        Text("No registered events", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Find events by IDs
                        val userEvents = events.filter { event -> 
                            user.registeredEvents.contains(event.id) 
                        }
                        
                        Column {
                            userEvents.forEach { event ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(event.title, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                dateFormat.format(event.startDate),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                // Unregister user from this event
                                                scope.launch {
                                                    try {
                                                        Log.d("AdminScreen", "Attempting to unregister user ${user.id} from event ${event.id}")
                                                        val success = firebaseService.unregisterUserFromEvent(user.id, event.id)
                                                        
                                                        // Even if API returns false, try to refresh the data
                                                        snackbarMessage = if (success) {
                                                            "User unregistered from event"
                                                        } else {
                                                            "Warning: May not have unregistered properly"
                                                        }
                                                        showSnackbar = true
                                                        
                                                        // Add delay to ensure Firebase operations complete
                                                        delay(1000)
                                                        
                                                        // Refresh data regardless of reported success
                                                        viewModel.loadUsers()
                                                        viewModel.loadEvents()
                                                        
                                                        // Update the displayed user
                                                        try {
                                                            val refreshedUser = firebaseService.getUserById(user.id)
                                                            if (refreshedUser != null) {
                                                                selectedUserForView = refreshedUser
                                                                snackbarMessage = "User data refreshed"
                                                            }
                                                        } catch (refreshError: Exception) {
                                                            Log.e("AdminScreen", "Failed to refresh user data: ${refreshError.message}")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("AdminScreen", "Error unregistering from event: ${e.message}", e)
                                                        snackbarMessage = "Error: ${e.message}"
                                                        showSnackbar = true
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveCircle,
                                                contentDescription = "Unregister from event",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Registered Hackathons section
                    Text("Registered Hackathons (${user.registeredHackathons.size})", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (user.registeredHackathons.isEmpty()) {
                        Text("No registered hackathons", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Find hackathons by IDs
                        val userHackathons = hackathons.filter { hackathon -> 
                            user.registeredHackathons.contains(hackathon.id) 
                        }
                        
                        Column {
                            userHackathons.forEach { hackathon ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(hackathon.title, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                dateFormat.format(hackathon.startDate),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                // Unregister user from this hackathon
                                                scope.launch {
                                                    try {
                                                        Log.d("AdminScreen", "Attempting to unregister user ${user.id} from hackathon ${hackathon.id}")
                                                        val success = firebaseService.unregisterUserFromHackathon(user.id, hackathon.id)
                                                        
                                                        // Even if API returns false, try to refresh the data
                                                        snackbarMessage = if (success) {
                                                            "User unregistered from hackathon"
                                                        } else {
                                                            "Warning: May not have unregistered properly"
                                                        }
                                                        showSnackbar = true
                                                        
                                                        // Add delay to ensure Firebase operations complete
                                                        delay(1000)
                                                        
                                                        // Refresh data regardless of reported success
                                                        viewModel.loadUsers()
                                                        viewModel.loadHackathons()
                                                        
                                                        // Update the displayed user
                                                        try {
                                                            val refreshedUser = firebaseService.getUserById(user.id)
                                                            if (refreshedUser != null) {
                                                                selectedUserForView = refreshedUser
                                                                snackbarMessage = "User data refreshed"
                                                            }
                                                        } catch (refreshError: Exception) {
                                                            Log.e("AdminScreen", "Failed to refresh user data: ${refreshError.message}")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("AdminScreen", "Error unregistering from hackathon: ${e.message}", e)
                                                        snackbarMessage = "Error: ${e.message}"
                                                        showSnackbar = true
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveCircle,
                                                contentDescription = "Unregister from hackathon",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showUserDetailsDialog = false
                    selectedUserForView = null
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Admin promotion confirmation dialog
    if (showPromoteAdminConfirmation && userToPromote != null) {
        AlertDialog(
            onDismissRequest = { 
                showPromoteAdminConfirmation = false
                userToPromote = null
            },
            title = { Text("Confirm Admin Promotion") },
            text = { 
                Text("Are you sure you want to promote ${userToPromote?.displayName} to admin?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToPromote?.let { user ->
                            scope.launch {
                                viewModel.promoteToAdmin(user.uid)
                            }
                            snackbarMessage = "User promoted to admin"
                            showSnackbar = true
                        }
                        showPromoteAdminConfirmation = false
                        userToPromote = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPromoteAdminConfirmation = false
                        userToPromote = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin demotion confirmation dialog
    if (showRemoveAdminConfirmation && userToDemote != null) {
        AlertDialog(
            onDismissRequest = { 
                showRemoveAdminConfirmation = false
                userToDemote = null
            },
            title = { Text("Confirm Admin Removal") },
            text = { 
                Text("Are you sure you want to remove admin privileges from ${userToDemote?.displayName}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToDemote?.let { user ->
                            scope.launch {
                                viewModel.removeAdminPrivileges(user.uid)
                            }
                            snackbarMessage = "Admin privileges removed"
                            showSnackbar = true
                        }
                        showRemoveAdminConfirmation = false
                        userToDemote = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRemoveAdminConfirmation = false
                        userToDemote = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonAdminCard(
    hackathon: Hackathon,
    onEdit: (Hackathon) -> Unit,
    onDelete: (Hackathon) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hackathon.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = { onEdit(hackathon) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { onDelete(hackathon) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Text("Date: ${dateFormat.format(hackathon.startDate)} - ${dateFormat.format(hackathon.endDate)}")
            Text("Location: ${hackathon.location}")
            Text("Prize Pool: ${hackathon.prizePool}")
        }
    }
}

@Composable
fun EventCard(event: Event, navController: NavController, viewModel: AdminViewModel, scope: CoroutineScope) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Event item content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (event.description.length > 50) 
                        event.description.take(50) + "..." 
                    else 
                        event.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row {
                IconButton(onClick = {
                    navController.navigate(Screen.EditEvent.createRoute(event.id))
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                
                IconButton(onClick = {
                    scope.launch {
                        viewModel.deleteEvent(event.id)
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun HackathonCard(hackathon: Hackathon, navController: NavController, viewModel: AdminViewModel, scope: CoroutineScope) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = hackathon.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (hackathon.description.length > 50) 
                        hackathon.description.take(50) + "..." 
                    else 
                        hackathon.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row {
                IconButton(onClick = {
                    navController.navigate(Screen.EditHackathon.createRoute(hackathon.id))
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                
                IconButton(onClick = {
                    scope.launch {
                        viewModel.deleteHackathon(hackathon.id)
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, navController: NavController, viewModel: AdminViewModel, scope: CoroutineScope) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.email, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (user.isAdmin) "Admin" else "Regular User",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Row {
                IconButton(onClick = {
                    navController.navigate("${Screen.UserDetailsScreen.route}/${user.id}")
                }) {
                    Icon(Icons.Default.Info, contentDescription = "Details")
                }
                
                if (user.email != "mistry.mitul1@gmail.com") {
                    if (user.isAdmin) {
                        // Show remove admin button
                        TextButton(
                            onClick = {
                                scope.launch {
                                    viewModel.removeAdminPrivileges(user.id)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("Remove Admin")
                        }
                    } else {
                        // Show make admin button
                        TextButton(
                            onClick = {
                                scope.launch {
                                    viewModel.promoteToAdmin(user.id)
                                }
                            }
                        ) {
                            Text("Make Admin")
                        }
                    }
                }
            }
        }
    }
} 