package com.mtl.My_Hack_X.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.mtl.My_Hack_X.components.DatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // State variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organizerName by remember { mutableStateOf("") }
    var registrationUrl by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var maxParticipantsText by remember { mutableStateOf("") }
    
    // Date state using Date object instead of string
    var eventDate by remember { mutableStateOf<Date>(Date()) }
    
    // Add an end date state
    var eventEndDate by remember { 
        mutableStateOf<Date>(
            Calendar.getInstance().apply {
                time = eventDate
                add(Calendar.DAY_OF_YEAR, 1) // Default end date is one day after start date
            }.time
        )
    }
    
    // Ensure end date is after start date
    LaunchedEffect(eventDate) {
        // If end date is before or equal to start date, set it to one day after
        if (eventEndDate.before(eventDate) || eventEndDate.equals(eventDate)) {
            val calendar = Calendar.getInstance()
            calendar.time = eventDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            eventEndDate = calendar.time
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date - Replace the text field with the DatePicker component
            DatePicker(
                selectedDate = eventDate,
                onDateSelected = { eventDate = it },
                label = "Event Date",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Add the end date picker after it
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add an end date state
            var eventEndDate by remember { 
                mutableStateOf<Date>(
                    Calendar.getInstance().apply {
                        time = eventDate
                        add(Calendar.DAY_OF_YEAR, 1) // Default end date is one day after start date
                    }.time
                )
            }
            
            // Ensure end date is after start date
            LaunchedEffect(eventDate) {
                // If end date is before or equal to start date, set it to one day after
                if (eventEndDate.before(eventDate) || eventEndDate.equals(eventDate)) {
                    val calendar = Calendar.getInstance()
                    calendar.time = eventDate
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    eventEndDate = calendar.time
                }
            }
            
            // End date picker
            DatePicker(
                selectedDate = eventEndDate,
                onDateSelected = { newEndDate ->
                    // Only accept end dates after the start date
                    if (newEndDate.after(eventDate)) {
                        eventEndDate = newEndDate 
                    }
                },
                label = "Event End Date",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Max Participants
            OutlinedTextField(
                value = maxParticipantsText,
                onValueChange = { maxParticipantsText = it },
                label = { Text("Max Participants") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Organizer Name
            OutlinedTextField(
                value = organizerName,
                onValueChange = { organizerName = it },
                label = { Text("Organizer Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Registration URL
            OutlinedTextField(
                value = registrationUrl,
                onValueChange = { registrationUrl = it },
                label = { Text("Registration URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image URL
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Success message
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Create button
            Button(
                onClick = {
                    // Validate inputs
                    if (title.isBlank() || description.isBlank() || location.isBlank() || organizerName.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }
                    
                    val maxParticipants = maxParticipantsText.toIntOrNull()
                    if (maxParticipantsText.isNotBlank() && maxParticipants == null) {
                        errorMessage = "Max participants must be a number"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    
                    coroutineScope.launch {
                        try {
                            // Create event object
                            val event = Event(
                                id = "",
                                title = title,
                                description = description,
                                startDate = eventDate,
                                endDate = eventEndDate,
                                location = location,
                                maxParticipants = maxParticipantsText.toIntOrNull() ?: 0,
                                currentParticipants = 0,
                                imageUrl = imageUrl,
                                organizerName = organizerName,
                                registrationUrl = registrationUrl,
                                registeredUsers = listOf()
                            )
                            
                            // Create event in Firestore
                            val createdEvent = firebaseService.createEvent(event)
                            
                            Log.d("CreateEventScreen", "Event created successfully: ${createdEvent.id}")
                            successMessage = "Event created successfully"
                            
                            // Reset form
                            title = ""
                            description = ""
                            eventDate = Date()
                            location = ""
                            maxParticipantsText = ""
                            organizerName = ""
                            registrationUrl = ""
                            imageUrl = ""
                            
                        } catch (e: Exception) {
                            Log.e("CreateEventScreen", "Error creating event: ${e.message}", e)
                            errorMessage = "Failed to create event: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Event")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 