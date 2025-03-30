package com.mtl.My_Hack_X.screens.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.model.EventType
import java.util.*
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (Event) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var maxParticipantsText by remember { mutableStateOf("50") } // Default value
    var organizer by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("https://picsum.photos/800/400") } // Default image
    
    // For simplicity, we're using current dates
    // In a real app, you'd use date pickers
    val currentDate = Date()
    val startDate = currentDate 
    val endDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) // One week later

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Event") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxParticipantsText,
                    onValueChange = { maxParticipantsText = it },
                    label = { Text("Max Participants") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Organizer") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date information
                Text(
                    text = "Event will be scheduled from now to one week later",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Log.d("AddEventDialog", "Creating new event with title: $title")
                    val maxParticipants = maxParticipantsText.toIntOrNull() ?: 50
                    
                    val event = Event(
                        id = "", // Will be set by Firestore
                        title = title,
                        description = description,
                        date = startDate, // Set both date fields
                        startDate = startDate,
                        endDate = endDate,
                        location = location,
                        maxParticipants = maxParticipants,
                        currentParticipants = 0,
                        organizer = organizer,
                        organizerName = organizer, // Set both organizer fields
                        registeredUsers = listOf(),
                        imageUrl = imageUrl,
                        status = EventStatus.UPCOMING,
                        type = EventType.WORKSHOP,
                        requirements = "",
                        tags = listOf(),
                        registrationUrl = "" // Set empty registration URL
                    )
                    
                    Log.d("AddEventDialog", "Event object created: $event")
                    onAddEvent(event)
                    Log.d("AddEventDialog", "onAddEvent callback completed")
                },
                enabled = title.isNotEmpty() && description.isNotEmpty() && 
                         location.isNotEmpty() && maxParticipantsText.isNotEmpty() &&
                         organizer.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 