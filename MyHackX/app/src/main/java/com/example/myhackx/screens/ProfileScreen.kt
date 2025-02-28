package com.example.myhackx.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myhackx.data.HackathonDataManager
import com.example.myhackx.data.UserManager
import com.example.myhackx.data.models.HackathonEvent
import com.example.myhackx.components.LoadingDialog
import com.example.myhackx.navigation.Screen
import com.example.myhackx.components.HackathonDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser by UserManager.currentUser.collectAsState()
    
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
        return
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedHackathon by remember { mutableStateOf<HackathonEvent?>(null) }
    var showUsersDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(if (currentUser?.isAdmin == true) "Admin Dashboard" else "Profile") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                UserManager.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
                if (currentUser?.isAdmin == true) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Events") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Users") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentUser?.isAdmin == true && selectedTab == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add Hackathon")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (currentUser?.isAdmin == true) {
                when (selectedTab) {
                    0 -> AdminEventsTab(
                        onEditHackathon = { selectedHackathon = it }
                    )
                    1 -> AdminUsersTab()
                }
            } else {
                UserProfile(currentUser?.email ?: "")
            }
        }

        // Dialogs
        if (showAddDialog || selectedHackathon != null) {
            HackathonDialog(
                hackathon = selectedHackathon,
                onDismiss = {
                    showAddDialog = false
                    selectedHackathon = null
                },
                onSave = { hackathon ->
                    if (selectedHackathon != null) {
                        HackathonDataManager.updateHackathon(hackathon)
                    } else {
                        HackathonDataManager.addHackathon(hackathon)
                    }
                    showAddDialog = false
                    selectedHackathon = null
                }
            )
        }
    }
}

@Composable
private fun AdminEventsTab(
    onEditHackathon: (HackathonEvent) -> Unit
) {
    LazyColumn {
        items(HackathonDataManager.hackathons) { hackathon ->
            AdminHackathonCard(
                hackathon = hackathon,
                onEdit = { onEditHackathon(hackathon) },
                onDelete = { HackathonDataManager.removeHackathon(hackathon.id) }
            )
        }
    }
}

@Composable
private fun AdminUsersTab() {
    LazyColumn {
        items(UserManager.users) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Registered Events: ${user.registeredEvents.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminHackathonCard(
    hackathon: HackathonEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            Text(
                text = hackathon.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Location: ${hackathon.location}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Prize Pool: ${hackathon.prizePool}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UserProfile(email: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = email,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Registered Events",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Show registered events here
        LazyColumn {
            items(UserManager.currentUser.value?.registeredEvents ?: emptyList()) { eventId ->
                val event = HackathonDataManager.hackathons.find { it.id == eventId }
                if (event != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Date: ${event.startDate} - ${event.endDate}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
} 