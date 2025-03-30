package com.mtl.My_Hack_X.screens.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "ErrorBoundary"

/**
 * A component that provides a controlled environment for exception handling
 * Note that Compose doesn't support direct try-catch for composition exceptions
 */
@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Log.d(TAG, "ErrorBoundary rendered")
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorCount by remember { mutableStateOf(0) }
    var lastRetriedAt by remember { mutableStateOf(0L) }
    
    // Create exception handler for coroutines
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Caught exception in coroutine: ${throwable.message}", throwable)
        hasError = true
        errorMessage = throwable.message ?: "Unknown error"
        errorCount++
    }
    
    val scope = rememberCoroutineScope { exceptionHandler }
    
    // Set up error state observer
    DisposableEffect(Unit) {
        Log.d(TAG, "ErrorBoundary entered")
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception: ${throwable.message}", throwable)
            if (!hasError) {
                hasError = true
                errorMessage = throwable.message ?: "Unknown error"
                errorCount++
            }
            
            // Don't call the original handler in production to prevent app crash
            // This is a trade-off: we lose crash reporting but maintain app stability
            if (errorCount > 3) {
                // If we've had too many errors, let the app crash to prevent infinite loops
                handler?.uncaughtException(thread, throwable)
            }
        }
        
        onDispose {
            Log.d(TAG, "ErrorBoundary exited")
            // Restore the original handler
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
    
    if (hasError) {
        Log.d(TAG, "Showing error screen: $errorMessage")
        ErrorScreen(
            errorMessage = errorMessage,
            errorCount = errorCount,
            onRetry = {
                Log.d(TAG, "Retry requested")
                val currentTime = System.currentTimeMillis()
                // Prevent rapid retries (must wait at least 1 second)
                if (currentTime - lastRetriedAt > 1000) {
                    lastRetriedAt = currentTime
                    hasError = false
                    // Don't reset errorCount to track consecutive failures
                }
            },
            modifier = modifier
        )
    } else {
        Log.d(TAG, "Rendering normal content")
        Box(modifier = modifier) {
            // Just render the content, we can't directly catch exceptions during composition
            content()
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    errorCount: Int = 0,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (errorCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Multiple errors detected ($errorCount). You may need to restart the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
} 