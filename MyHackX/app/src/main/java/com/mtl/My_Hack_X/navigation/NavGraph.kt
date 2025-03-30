package com.mtl.My_Hack_X.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mtl.My_Hack_X.screens.*

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Screen.Home.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Intro screens
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        // Main screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.Admin.route) {
            AdminScreen(navController = navController)
        }
        
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        
        composable(Screen.Hackathons.route) {
            HackathonsScreen(navController = navController)
        }
        
        composable(Screen.Events.route) {
            EventsScreen(navController = navController)
        }
        
        // Detail screens
        composable(
            route = Screen.HackathonDetails.route,
            arguments = listOf(
                navArgument("hackathonId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val hackathonId = backStackEntry.arguments?.getString("hackathonId") ?: ""
            HackathonDetailsScreen(navController = navController, hackathonId = hackathonId)
        }
        
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(navController = navController, eventId = eventId)
        }
        
        // Admin screens
        composable(Screen.AddEvent.route) {
            CreateEventScreen(navController = navController)
        }
        
        composable(Screen.AddHackathon.route) {
            CreateHackathonScreen(navController = navController)
        }
        
        composable(Screen.AddUser.route) {
            AddUserScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EditEventScreen(navController = navController, eventId = eventId)
        }
        
        composable(
            route = Screen.EditHackathon.route,
            arguments = listOf(
                navArgument("hackathonId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val hackathonId = backStackEntry.arguments?.getString("hackathonId") ?: ""
            EditHackathonScreen(navController = navController, hackathonId = hackathonId)
        }
        
        composable(
            route = Screen.EditUser.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditUserScreen(navController = navController, userId = userId)
        }
        
        // Add registration form route to the NavGraph
        // Add after the existing routes before the end of the NavHost
        
        composable(
            route = Screen.RegistrationForm.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("isHackathon") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val isHackathon = backStackEntry.arguments?.getBoolean("isHackathon") ?: false
            RegistrationFormScreen(
                navController = navController,
                eventId = eventId,
                isHackathon = isHackathon
            )
        }
    }
} 