package com.example.myhackx.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HackathonDetailsScreen(
    navController: NavController,
    hackathonId: String
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Hackathon Details: $hackathonId")
        // Add more UI components here as needed
    }
} 