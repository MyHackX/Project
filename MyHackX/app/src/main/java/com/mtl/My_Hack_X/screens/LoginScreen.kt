package com.mtl.My_Hack_X.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mtl.My_Hack_X.data.UserManager
import com.mtl.My_Hack_X.data.services.FirebaseService
import com.mtl.My_Hack_X.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val firebaseService = remember { FirebaseService.getInstance() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Check if user is already logged in
    LaunchedEffect(Unit) {
        if (firebaseService.isUserLoggedIn()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // Google Sign In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                Log.d("LoginScreen", "Google Sign In result received, resultCode: ${result.resultCode}")
                if (result.data == null) {
                    Log.e("LoginScreen", "Google Sign In failed: result data is null")
                    errorMessage = "Google Sign In failed: No data returned"
                    isLoading = false
                    return@launch
                }
                
                Log.d("LoginScreen", "Attempting to process Google Sign In result")
                val signInResult = firebaseService.handleGoogleSignInResult(result.data)
                signInResult.fold(
                    onSuccess = { userId ->
                        Log.d("LoginScreen", "Google Sign In successful for user ID: $userId")
                        
                        // Double-check user is actually signed in
                        if (firebaseService.isUserLoggedIn()) {
                            Log.d("LoginScreen", "User confirmed logged in, navigating to Home")
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            Log.e("LoginScreen", "User not logged in despite successful result")
                            errorMessage = "Authentication succeeded but login failed. Please try again."
                            isLoading = false
                        }
                    },
                    onFailure = { exception ->
                        Log.e("LoginScreen", "Google Sign In failed: ${exception.message}", exception)
                        errorMessage = exception.message ?: "Google Sign In failed. Please try again."
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginScreen", "Exception during Google Sign In: ${e.message}", e)
                errorMessage = e.message ?: "Google Sign In failed. Please try again."
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Login form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // App logo or title
                Text(
                    text = "My Hack X",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                
                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Forgot password
                TextButton(
                    onClick = { /* TODO: Implement forgot password functionality */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password cannot be empty"
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            
                            val result = firebaseService.signIn(email, password)
                            result.fold(
                                onSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onFailure = { exception ->
                                    errorMessage = "Login failed: ${exception.message}"
                                    isLoading = false
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Login")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Or divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Google Sign In Button
                OutlinedButton(
                    onClick = {
                        try {
                            Log.d("LoginScreen", "Initiating Google Sign In")
                            val signInIntent = firebaseService.getSignInIntent(context)
                            Log.d("LoginScreen", "Got sign-in intent, launching activity")
                            googleSignInLauncher.launch(signInIntent)
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Error starting Google Sign In: ${e.message}", e)
                            errorMessage = "Error starting Google Sign In: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Google",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Google")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sign up link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Don't have an account?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                        Text("Sign Up")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Loading overlay
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
} 