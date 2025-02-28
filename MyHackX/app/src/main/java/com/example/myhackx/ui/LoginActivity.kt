package com.example.myhackx.ui

import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myhackx.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Connect the Sign Up text click
        findViewById<TextView>(R.id.tvSignUp).setOnClickListener {
            // Simple navigation without flags
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()  // Just finish this activity
        }
    }

    // Example button click
    fun onSignUpClick() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
} 