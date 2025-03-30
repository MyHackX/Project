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
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.mtl.My_Hack_X.components.DatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHackathonScreen(navController: NavController) {
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
    var prizePool by remember { mutableStateOf("") }
    
    // Date state using Date object instead of string
    var hackathonDate by remember { mutableStateOf<Date>(Date()) }
    
    // Add the end date state
    var hackathonEndDate by remember { 
        mutableStateOf<Date>(
            Calendar.getInstance().apply {
                time = hackathonDate
                add(Calendar.DAY_OF_YEAR, 2) // Default end date is two days after start date for hackathons
            }.time
        )
    }
    
    // Ensure end date is after start date
    LaunchedEffect(hackathonDate) {
        // If end date is before or equal to start date, set it to two days after
        if (hackathonEndDate.before(hackathonDate) || hackathonEndDate.equals(hackathonDate)) {
            val calendar = Calendar.getInstance()
            calendar.time = hackathonDate
            calendar.add(Calendar.DAY_OF_YEAR, 2)
            hackathonEndDate = calendar.time
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Hackathon") },
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
                label = { Text("Hackathon Title") },
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
                selectedDate = hackathonDate,
                onDateSelected = { hackathonDate = it },
                label = "Hackathon Date",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Add the end date picker after it
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add an end date state
            var hackathonEndDate by remember { 
                mutableStateOf<Date>(
                    Calendar.getInstance().apply {
                        time = hackathonDate
                        add(Calendar.DAY_OF_YEAR, 2) // Default end date is two days after start date for hackathons
                    }.time
                )
            }
            
            // Ensure end date is after start date
            LaunchedEffect(hackathonDate) {
                // If end date is before or equal to start date, set it to two days after
                if (hackathonEndDate.before(hackathonDate) || hackathonEndDate.equals(hackathonDate)) {
                    val calendar = Calendar.getInstance()
                    calendar.time = hackathonDate
                    calendar.add(Calendar.DAY_OF_YEAR, 2)
                    hackathonEndDate = calendar.time
                }
            }
            
            // End date picker
            DatePicker(
                selectedDate = hackathonEndDate,
                onDateSelected = { newEndDate ->
                    // Only accept end dates after the start date
                    if (newEndDate.after(hackathonDate)) {
                        hackathonEndDate = newEndDate 
                    }
                },
                label = "Hackathon End Date",
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
            
            // Prize Pool
            OutlinedTextField(
                value = prizePool,
                onValueChange = { prizePool = it },
                label = { Text("Prize Pool (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                            // Create hackathon object
                            val hackathon = Hackathon(
                                id = "",
                                title = title,
                                description = description,
                                startDate = hackathonDate,
                                endDate = hackathonEndDate,
                                location = location,
                                maxParticipants = maxParticipantsText.toIntOrNull() ?: 0,
                                currentParticipants = 0,
                                prizePool = prizePool,
                                imageUrl = imageUrl,
                                organizerName = organizerName,
                                registrationUrl = registrationUrl,
                                registeredUsers = listOf()
                            )
                            
                            // Create hackathon in Firestore
                            val createdHackathon = firebaseService.createHackathon(hackathon)
                            
                            Log.d("CreateHackathonScreen", "Hackathon created successfully: ${createdHackathon.id}")
                            successMessage = "Hackathon created successfully"
                            
                            // Reset form
                            title = ""
                            description = ""
                            hackathonDate = Date()
                            hackathonEndDate = Date()
                            location = ""
                            maxParticipantsText = ""
                            prizePool = ""
                            organizerName = ""
                            registrationUrl = ""
                            imageUrl = ""
                            
                        } catch (e: Exception) {
                            Log.e("CreateHackathonScreen", "Error creating hackathon: ${e.message}", e)
                            errorMessage = "Failed to create hackathon: ${e.message}"
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
                    Text("Create Hackathon")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 