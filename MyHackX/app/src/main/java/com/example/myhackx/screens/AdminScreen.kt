package com.example.myhackx.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myhackx.data.DatabaseHelper
import com.example.myhackx.data.models.HackathonEvent
import com.example.myhackx.components.HackathonDialog
import com.example.myhackx.data.UserManager
import com.example.myhackx.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val currentUser by UserManager.currentUser.collectAsState()
    
    // Redirect non-admin users to home
    LaunchedEffect(currentUser) {
        currentUser?.email?.let { email ->
            if (!UserManager.isAdmin(email)) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Admin.route) { inclusive = true }
                }
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingHackathon by remember { mutableStateOf<HackathonEvent?>(null) }
    
    val hackathons by remember { 
        mutableStateOf(DatabaseHelper.getAllHackathons()) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Event")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(hackathons) { hackathon ->
                HackathonAdminCard(
                    hackathon = hackathon,
                    onEdit = { editingHackathon = it },
                    onDelete = { 
                        DatabaseHelper.deleteHackathon(it.name)
                    }
                )
            }
        }

        if (showAddDialog) {
            HackathonDialog(
                hackathon = null,
                onDismiss = { showAddDialog = false },
                onSave = { newHackathon ->
                    DatabaseHelper.addHackathon(newHackathon)
                    showAddDialog = false
                }
            )
        }

        editingHackathon?.let { hackathon ->
            HackathonDialog(
                hackathon = hackathon,
                onDismiss = { editingHackathon = null },
                onSave = { updatedHackathon ->
                    DatabaseHelper.updateHackathon(updatedHackathon)
                    editingHackathon = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonAdminCard(
    hackathon: HackathonEvent,
    onEdit: (HackathonEvent) -> Unit,
    onDelete: (HackathonEvent) -> Unit
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
                    text = hackathon.name,
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
            Text("Date: ${hackathon.startDate} - ${hackathon.endDate}")
            Text("Location: ${hackathon.location}")
            Text("Prize Pool: ${hackathon.prizePool}")
        }
    }
} 