package com.mtl.My_Hack_X.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.model.Event
import com.mtl.My_Hack_X.data.model.Hackathon
import com.mtl.My_Hack_X.data.model.HackathonStatus
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.CardDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun HomeScreen(navController: NavController) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val scope = rememberCoroutineScope()
    
    // State for current user
    var isAdmin by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var hackathons by remember { mutableStateOf<List<Hackathon>>(emptyList()) }
    
    // Function to load data
    val loadData = {
        scope.launch {
            try {
                Log.d("HomeScreen", "Loading events and hackathons")
                // Load events first
                val loadedEvents = firebaseService.getAllEvents()
                Log.d("HomeScreen", "Loaded ${loadedEvents.size} events")
                events = loadedEvents
                
                // Then load hackathons
                val loadedHackathons = firebaseService.getAllHackathons()
                Log.d("HomeScreen", "Loaded ${loadedHackathons.size} hackathons")
                hackathons = loadedHackathons
                
                isLoading = false
                isRefreshing = false
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error loading data: ${e.message}")
                isLoading = false
                isRefreshing = false
            }
        }
    }
    
    // Pull to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            loadData()
        }
    )
    
    // Check login status and load data
    LaunchedEffect(Unit) {
        isLoading = true
        
        // Check if user is logged in
        val userId = firebaseService.getCurrentUserId()
        isLoggedIn = userId != null
        
        // Check if user is an admin
        if (isLoggedIn && userId != null) {
            try {
                val user = firebaseService.getUserById(userId)
                isAdmin = user?.isAdmin ?: false
            } catch (e: Exception) {
                isAdmin = false
            }
        }
        
        // Load data
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Hack X") },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { navController.navigate(Screen.Admin.route) }) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel")
                        }
                    }
                    IconButton(onClick = { 
                        if (isLoggedIn) {
                            navController.navigate(Screen.Profile.route)
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    }) {
                        Icon(
                            if (isLoggedIn) Icons.Default.Person else Icons.Default.Login, 
                            contentDescription = if (isLoggedIn) "Profile" else "Login"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Upcoming Hackathons",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (hackathons.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No upcoming hackathons at the moment",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(hackathons) { hackathon ->
                                    HackathonCardCompact(
                                        hackathon = hackathon,
                                        onClick = {
                                            navController.navigate(Screen.HackathonDetails.createRoute(hackathon.id))
                                        }
                                    )
                                }
                            }
                            
                            TextButton(
                                onClick = { navController.navigate(Screen.Hackathons.route) },
                                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
                            ) {
                                Text("View All")
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "View All",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                    
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Latest Events",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (events.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No upcoming events at the moment",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    } else {
                        items(events) { event ->
                            EventCardCompact(
                                event = event,
                                onClick = {
                                    navController.navigate(Screen.EventDetails.createRoute(event.id))
                                }
                            )
                        }
                    }
                }
            }
            
            // Pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonCardCompact(
    hackathon: Hackathon,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = hackathon.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = hackathon.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
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
                    text = dateFormat.format(hackathon.startDate),
                    style = MaterialTheme.typography.bodySmall
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
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                
                Text(
                    text = "${hackathon.currentParticipants}/${hackathon.maxParticipants}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCardCompact(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
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
                        text = formatDate(event.startDate),
                        style = MaterialTheme.typography.bodySmall
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
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "View Details"
                )
            }
        }
    }
}

// Helper function to format date
private fun formatDate(date: Date): String {
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return dateFormat.format(date)
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "About HackX",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = { 
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "HackX connects talented individuals with exciting hackathon opportunities around the world.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Team Members:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Dhruvil") },
                        supportingContent = { Text("Lead Developer") }
                    )
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Mitul") },
                        supportingContent = { Text("UI/UX Designer") }
                    )
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Kathan") },
                        supportingContent = { Text("Backend Developer") }
                    )
                }
            }
        },
        confirmButton = { 
            TextButton(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonCard(
    hackathon: Hackathon,
    context: Context = LocalContext.current,
    onClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val firebaseService = remember { FirebaseService.getInstance() }
    var isExpanded by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded) 12.dp else 4.dp,
        label = "cardElevation"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable { 
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) { // Double click detected
                    // Handle double click
                }
                lastClickTime = currentTime
                isExpanded = !isExpanded 
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
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
                    text = hackathon.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${formatDate(hackathon.startDate)} - ${formatDate(hackathon.endDate)}")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(hackathon.location)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Prize",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(hackathon.prizePool)
                    }
                }
            }
        }
    }
} 