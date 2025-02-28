package com.example.myhackx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myhackx.data.DatabaseHelper
import com.example.myhackx.data.UserManager
import com.example.myhackx.navigation.Screen
import com.example.myhackx.screens.*
import com.example.myhackx.ui.theme.MyHackXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database
        DatabaseHelper.init(applicationContext)
        UserManager.init(DatabaseHelper)
        
        setContent {
            MyHackXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) { 
                            SplashScreen(navController) 
                        }
                        composable(Screen.Welcome.route) { 
                            WelcomeScreen(navController) 
                        }
                        composable(Screen.Login.route) { 
                            LoginScreen(navController) 
                        }
                        composable(Screen.SignUp.route) { 
                            SignUpScreen(navController) 
                        }
                        composable(Screen.Home.route) { 
                            HomeScreen(navController) 
                        }
                        composable(Screen.Profile.route) { 
                            ProfileScreen(navController) 
                        }
                        composable(Screen.Admin.route) { AdminScreen(navController) }
                    }
                }
            }
        }
    }
}