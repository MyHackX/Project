package com.mtl.My_Hack_X.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtl.My_Hack_X.data.model.HackathonEvent
import com.google.firebase.Timestamp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonDialog(
    hackathon: HackathonEvent? = null,
    onDismiss: () -> Unit,
    onSave: (HackathonEvent) -> Unit
) {
    var name by remember { mutableStateOf(hackathon?.name ?: "") }
    var description by remember { mutableStateOf(hackathon?.description ?: "") }
    var location by remember { mutableStateOf(hackathon?.location ?: "") }
    var maxParticipants by remember { mutableStateOf(hackathon?.maxParticipants?.toString() ?: "") }
    var prizePool by remember { mutableStateOf(hackathon?.prizePool ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (hackathon == null) "Create New Hackathon" else "Edit Hackathon") 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Hackathon Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it.filter { char -> char.isDigit() } },
                    label = { Text("Maximum Participants") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prizePool,
                    onValueChange = { prizePool = it },
                    label = { Text("Prize Pool") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newHackathon = HackathonEvent(
                        id = hackathon?.id ?: "",
                        name = name,
                        description = description,
                        startDate = hackathon?.startDate ?: Timestamp.now(),
                        endDate = hackathon?.endDate ?: Timestamp(Date(System.currentTimeMillis() + 86400000)),
                        location = location,
                        maxParticipants = maxParticipants.toIntOrNull() ?: 0,
                        prizePool = prizePool,
                        organizer = hackathon?.organizer ?: "",
                        registrationDeadline = hackathon?.registrationDeadline ?: Timestamp.now()
                    )
                    onSave(newHackathon)
                    onDismiss()
                },
                enabled = name.isNotBlank() && description.isNotBlank() && 
                         location.isNotBlank() && maxParticipants.isNotBlank()
            ) {
                Text(if (hackathon == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}