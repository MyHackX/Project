package com.example.myhackx.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myhackx.data.models.HackathonEvent
import com.example.myhackx.data.RegistrationData
import com.example.myhackx.data.DatabaseHelper
import com.example.myhackx.utils.EmailSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationDialog(
    hackathon: HackathonEvent,
    userId: String,
    userName: String,
    onDismiss: () -> Unit,
    onRegistered: () -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var mobile by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var currentEducation by remember { mutableStateOf("") }
    var isGraduate by remember { mutableStateOf(false) }
    var field by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register for ${hackathon.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College/University *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = currentEducation,
                    onValueChange = { currentEducation = it },
                    label = { Text("Current Education *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = isGraduate,
                        onCheckedChange = { isGraduate = it }
                    )
                    Text("Are you a graduate?")
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = field,
                    onValueChange = { field = it },
                    label = { Text("Field of Study/Expertise *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    label = { Text("Additional Information") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Please enter your name"
                        mobile.isBlank() -> error = "Please enter your mobile number"
                        college.isBlank() -> error = "Please enter your college/university"
                        currentEducation.isBlank() -> error = "Please enter your current education"
                        field.isBlank() -> error = "Please enter your field"
                        else -> {
                            val registration = RegistrationData(
                                userId = userId,
                                hackathonName = hackathon.name,
                                name = name,
                                mobileNumber = mobile,
                                college = college,
                                currentEducation = currentEducation,
                                isGraduate = isGraduate,
                                field = field,
                                additionalInfo = additionalInfo
                            )
                            
                            // Save registration
                            DatabaseHelper.addRegistration(registration)
                            
                            // Send email to admin
                            EmailSender.sendRegistrationNotificationToAdmin(
                                context = context,
                                registration = registration,
                                hackathon = hackathon
                            )
                            
                            onRegistered()
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 