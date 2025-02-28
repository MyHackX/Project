package com.example.myhackx.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myhackx.screens.LoginScreen
import com.example.myhackx.screens.AdminScreen
import com.example.myhackx.screens.HomeScreen
import com.example.myhackx.screens.RegistrationScreen
import com.example.myhackx.screens.HackathonDetailsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(navController)
        }
        composable(Screen.Admin.route) {
            AdminScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.HackathonDetails.route,
            arguments = listOf(navArgument("hackathonId") { type = NavType.StringType })
        ) { backStackEntry ->
            HackathonDetailsScreen(
                navController = navController,
                hackathonId = backStackEntry.arguments?.getString("hackathonId") ?: ""
            )
        }
        // ... other routes
    }
} 