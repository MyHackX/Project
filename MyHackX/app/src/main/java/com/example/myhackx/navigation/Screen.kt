package com.example.myhackx.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object About : Screen("about")
    object Admin : Screen("admin")
    object Registration : Screen("registration")
    object HackathonDetails : Screen("hackathon_details/{hackathonId}") {
        fun createRoute(hackathonId: String) = "hackathon_details/$hackathonId"
    }
} 