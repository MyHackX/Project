package com.example.myhackx.ui

import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myhackx.R
import com.example.myhackx.utils.EmailService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private val emailService = EmailService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Connect the Login text click
        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            // Simple navigation without flags
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  // Just finish this activity
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Simple navigation on back press
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun handleRegistration(userEmail: String, username: String) {
        lifecycleScope.launch {
            try {
                emailService.sendWelcomeEmailToUser(userEmail, username)
                Toast.makeText(
                    this@RegisterActivity,
                    "Account created successfully! Please check your email.",
                    Toast.LENGTH_LONG
                ).show()
                // After successful registration, go to login
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Account created but failed to send email: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Example of how to call handleRegistration when user submits the form
    private fun onRegisterButtonClick() {
        // Replace these with actual values from your form
        val userEmail = "user@example.com"  // Get from email input field
        val userName = "username"           // Get from username input field
        handleRegistration(userEmail, userName)
    }
} 