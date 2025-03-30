package com.mtl.My_Hack_X.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.EventStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    
    // State variables
    var isLoading by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var filteredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(EventFilter.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Load events
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            events = firebaseService.getAllEvents()
            filteredEvents = events
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    // Filter events based on search query and filter selection
    LaunchedEffect(searchQuery, selectedFilter, events) {
        filteredEvents = events.filter { event ->
            val matchesQuery = event.title.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true) ||
                    event.location.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when (selectedFilter) {
                EventFilter.ALL -> true
                EventFilter.UPCOMING -> event.status == EventStatus.UPCOMING
                EventFilter.ONGOING -> event.status == EventStatus.ONGOING
                EventFilter.COMPLETED -> event.status == EventStatus.COMPLETED
            }
            
            matchesQuery && matchesFilter
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Events") },
            text = {
                Column {
                    EventFilter.values().forEach { filter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFilter = filter
                                    showFilterDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFilter == filter,
                                onClick = {
                                    selectedFilter = filter
                                    showFilterDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(filter.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search events...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true
            )
            
            // Filter chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FilterChip(
                    selected = false,
                    onClick = { showFilterDialog = true },
                    label = { Text(selectedFilter.displayName) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredEvents) { event ->
                        EventCard(
                            event = event,
                            onClick = {
                                navController.navigate(Screen.EventDetails.createRoute(event.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.currentParticipants}/${event.maxParticipants}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                SuggestionChip(
                    onClick = { },
                    label = { Text(event.status.name) }
                )
            }
        }
    }
}

enum class EventFilter(val displayName: String) {
    ALL("All Events"),
    UPCOMING("Upcoming"),
    ONGOING("Ongoing"),
    COMPLETED("Completed")
} 