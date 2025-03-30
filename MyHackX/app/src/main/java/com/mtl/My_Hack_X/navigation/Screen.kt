package com.mtl.My_Hack_X.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object AdminDashboard : Screen("admin_dashboard")
    object About : Screen("about")
    
    object Hackathons : Screen("hackathons")
    object Events : Screen("events")
    
    object HackathonDetails : Screen("hackathon_details/{hackathonId}") {
        fun createRoute(hackathonId: String): String {
            return "hackathon_details/$hackathonId"
        }
    }
    
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String): String {
            return "event_details/$eventId"
        }
    }
    
    object AddEvent : Screen("add_event")
    object AddHackathon : Screen("add_hackathon")
    object AddUser : Screen("add_user")
    
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String): String {
            return "edit_event/$eventId"
        }
    }
    
    object EditHackathon : Screen("edit_hackathon/{hackathonId}") {
        fun createRoute(hackathonId: String): String {
            return "edit_hackathon/$hackathonId"
        }
    }
    
    object EditUser : Screen("edit_user/{userId}") {
        fun createRoute(userId: String): String {
            return "edit_user/$userId"
        }
    }
    
    object UserDetailsScreen : Screen("user_details/{userId}") {
        fun createRoute(userId: String): String {
            return "user_details/$userId"
        }
    }

    object RegistrationForm : Screen("registration/{eventId}/{isHackathon}") {
        fun createRoute(eventId: String, isHackathon: Boolean): String {
            return "registration/$eventId/$isHackathon"
        }
    }

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                "welcome" -> Welcome
                "login" -> Login
                "signup" -> SignUp
                "register" -> Register
                "home" -> Home
                "profile" -> Profile
                "admin" -> Admin
                "admin_dashboard" -> AdminDashboard
                "about" -> About
                "hackathons" -> Hackathons
                "hackathon" -> HackathonDetails
                "event" -> EventDetails
                "add_event" -> AddEvent
                "add_hackathon" -> AddHackathon
                "add_user" -> AddUser
                "edit_event" -> EditEvent
                "edit_hackathon" -> EditHackathon
                "edit_user" -> EditUser
                "user_details" -> UserDetailsScreen
                "registration" -> RegistrationForm
                null -> Home
                else -> throw IllegalArgumentException("Route $route is not recognized")
            }
        }
    }
} 