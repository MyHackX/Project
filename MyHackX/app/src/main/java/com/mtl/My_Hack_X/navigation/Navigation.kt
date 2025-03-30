package com.mtl.My_Hack_X.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mtl.My_Hack_X.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.About.route) { AboutScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Hackathons.route) { HackathonsScreen(navController) }
        composable(Screen.Admin.route) { AdminScreen(navController) }
        composable(Screen.Events.route) { EventsScreen(navController) }
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
    }
} 