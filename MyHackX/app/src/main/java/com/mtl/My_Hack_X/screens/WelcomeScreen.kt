package com.mtl.My_Hack_X.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.R
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    // Animation states
    var animationPlayed by remember { mutableStateOf(false) }
    var currentFeatureIndex by remember { mutableStateOf(0) }
    
    val features = listOf(
        Feature(
            title = "Discover Hackathons",
            description = "Find exciting hackathons and tech events happening around you",
            icon = Icons.Default.Explore
        ),
        Feature(
            title = "Register & Participate",
            description = "Easily register for events and collaborate with other participants",
            icon = Icons.Default.Groups
        ),
        Feature(
            title = "Win Prizes",
            description = "Showcase your skills and win amazing prizes",
            icon = Icons.Default.EmojiEvents
        )
    )
    
    // Feature animation
    val featureAlpha by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )
    
    // Button animations
    val buttonScale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Auto-cycle through features
    LaunchedEffect(key1 = true) {
        animationPlayed = true
        
        // Auto-scroll through features
        while (true) {
            delay(3000) // Show each feature for 3 seconds
            currentFeatureIndex = (currentFeatureIndex + 1) % features.size
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyHackX") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Animated Welcome Text
            androidx.compose.animation.AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn() + expandVertically()
            ) {
                Text(
                    text = "Welcome to MyHackX",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated Tagline
            androidx.compose.animation.AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(initialAlpha = 0f) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 800, delayMillis = 300)
                )
            ) {
                Text(
                    text = "Your platform for discovering and participating in exciting hackathons",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Feature Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
                    .graphicsLayer(alpha = featureAlpha)
            ) {
                // Animated Feature Cards
                features.forEachIndexed { index, feature ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = currentFeatureIndex == index,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.title,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = feature.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = feature.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                
                // Feature Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    features.indices.forEach { index ->
                        val color = if (currentFeatureIndex == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        }
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Login Button with Animation
            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = "Login")
                    Text("Login", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sign Up Button with Animation
            OutlinedButton(
                onClick = { navController.navigate(Screen.SignUp.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Sign Up")
                    Text("Sign Up", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
} 