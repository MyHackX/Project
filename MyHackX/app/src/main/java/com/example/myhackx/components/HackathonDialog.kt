package com.example.myhackx.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myhackx.data.models.HackathonEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonDialog(
    hackathon: HackathonEvent?,
    onDismiss: () -> Unit,
    onSave: (HackathonEvent) -> Unit
) {
    var name by remember { mutableStateOf(hackathon?.name ?: "") }
    var startDate by remember { mutableStateOf(hackathon?.startDate ?: "") }
    var endDate by remember { mutableStateOf(hackathon?.endDate ?: "") }
    var location by remember { mutableStateOf(hackathon?.location ?: "") }
    var prizePool by remember { mutableStateOf(hackathon?.prizePool ?: "") }
    var description by remember { mutableStateOf(hackathon?.description ?: "") }
    var registrationUrl by remember { mutableStateOf(hackathon?.registrationUrl ?: "") }
    var organizer by remember { mutableStateOf(hackathon?.organizer ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (hackathon == null) "Add Hackathon" else "Edit Hackathon") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prizePool,
                    onValueChange = { prizePool = it },
                    label = { Text("Prize Pool") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = registrationUrl,
                    onValueChange = { registrationUrl = it },
                    label = { Text("Registration URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Organizer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newHackathon = HackathonEvent(
                        id = hackathon?.id ?: "",
                        name = name,
                        startDate = startDate,
                        endDate = endDate,
                        location = location,
                        prizePool = prizePool,
                        description = description,
                        registrationOpen = true,
                        imageUrl = hackathon?.imageUrl ?: "",
                        registrationUrl = registrationUrl,
                        organizer = organizer
                    )
                    onSave(newHackathon)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}