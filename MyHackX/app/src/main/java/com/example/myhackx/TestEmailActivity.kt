package com.example.myhackx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.myhackx.utils.EmailService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TestEmailActivity : AppCompatActivity() {
    private val TAG = "TestEmailActivity"
    private val emailService = EmailService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_email)

        findViewById<Button>(R.id.btnTestEmail).setOnClickListener {
            testEmail()
        }
    }

    private fun testEmail() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting email test...")
                emailService.sendWelcomeEmailToUser(
                    "recipient@gmail.com",  // Replace with test email
                    "Test User"
                )
                Log.d(TAG, "Email sent successfully!")
                Toast.makeText(this@TestEmailActivity, "Email sent!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Email error: ${e.message}", e)
                Toast.makeText(
                    this@TestEmailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
} 