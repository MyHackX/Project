package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonsScreen(navController: NavController) {
    val firebaseService = remember { FirebaseService.getInstance() }
    
    // State
    var hackathons by remember { mutableStateOf<List<Hackathon>>(emptyList()) }
    var filteredHackathons by remember { mutableStateOf<List<Hackathon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    val filterOptions = listOf("All", "Upcoming", "Ongoing", "Completed")
    
    // Load hackathons
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val result = firebaseService.getAllHackathons()
            hackathons = result
            filteredHackathons = result
            isLoading = false
        } catch (e: Exception) {
            // Handle error
            isLoading = false
        }
    }
    
    // Filter hackathons when search query or filter changes
    LaunchedEffect(searchQuery.text, selectedFilter, hackathons) {
        filteredHackathons = hackathons.filter { hackathon ->
            // Apply search filter
            val matchesSearch = hackathon.title.contains(searchQuery.text, ignoreCase = true) ||
                                hackathon.description.contains(searchQuery.text, ignoreCase = true) ||
                                hackathon.location.contains(searchQuery.text, ignoreCase = true)
            
            // Apply status filter
            val matchesStatus = when (selectedFilter) {
                "Upcoming" -> hackathon.status == HackathonStatus.UPCOMING
                "Ongoing" -> hackathon.status == HackathonStatus.ONGOING
                "Completed" -> hackathon.status == HackathonStatus.COMPLETED
                else -> true // "All" option
            }
            
            matchesSearch && matchesStatus
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hackathons") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search hackathons") },
                leadingIcon = { 
                    Icon(Icons.Default.Search, contentDescription = "Search") 
                },
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // Selected filter chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                FilterChip(
                    selected = true,
                    onClick = { showFilterDialog = true },
                    label = { Text(selectedFilter) },
                    leadingIcon = { 
                        Icon(
                            imageVector = when (selectedFilter) {
                                "Upcoming" -> Icons.Default.Upcoming
                                "Ongoing" -> Icons.Default.Event
                                "Completed" -> Icons.Default.EventAvailable
                                else -> Icons.Default.EventNote
                            },
                            contentDescription = null
                        )
                    }
                )
            }
            
            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredHackathons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hackathons found",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (searchQuery.text.isEmpty()) 
                                "There are no hackathons available at the moment."
                            else
                                "Try adjusting your search or filters.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredHackathons.size) { index ->
                        val hackathon = filteredHackathons[index]
                        HackathonCard(
                            hackathon = hackathon,
                            onClick = {
                                navController.navigate(Screen.HackathonDetails.createRoute(hackathon.id))
                            }
                        )
                    }
                }
            }
        }
        
        // Filter dialog
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filter Hackathons") },
                text = {
                    Column {
                        filterOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        selectedFilter = option
                                        showFilterDialog = false
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFilter == option,
                                    onClick = {
                                        selectedFilter = option
                                        showFilterDialog = false
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(option)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonCard(hackathon: Hackathon, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                    text = hackathon.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                AssistChip(
                    onClick = { },
                    label = { Text(hackathon.status.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (hackathon.status) {
                            HackathonStatus.UPCOMING -> MaterialTheme.colorScheme.primaryContainer
                            HackathonStatus.ONGOING -> MaterialTheme.colorScheme.secondaryContainer
                            HackathonStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                            HackathonStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Hackathon details
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "${dateFormat.format(hackathon.startDate)} - ${dateFormat.format(hackathon.endDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = hackathon.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Prize: ${hackathon.prizePool}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = hackathon.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${hackathon.currentParticipants}/${hackathon.maxParticipants} participants",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                TextButton(onClick = onClick) {
                    Text("View Details")
                }
            }
        }
    }
} 