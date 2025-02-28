package com.example.myhackx.utils

import android.util.Log
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailService private constructor() {
    private val TAG = "EmailService"

    companion object {
        private const val HOST = "smtp.gmail.com"
        private const val PORT = "587"
        // Hardcoded admin email
        private const val ADMIN_EMAIL = "dhruvilpatel10102005@gmail.com"
        private const val ADMIN_PASSWORD = "your-16-digit-app-password" // Replace with your app password
        private const val ADMIN_NAME = "HackX Admin"

        // Singleton instance
        @Volatile
        private var instance: EmailService? = null

        fun getInstance(): EmailService {
            return instance ?: synchronized(this) {
                instance ?: EmailService().also { instance = it }
            }
        }
    }

    suspend fun sendWelcomeEmailToUser(userEmail: String, username: String) {
        Log.d(TAG, "Preparing to send welcome email to: $userEmail")
        
        val subject = "Welcome to HackX!"
        val message = """
            Hi $username,
            
            Welcome to HackX! Your account has been successfully created.
            
            You can now:
            - Browse upcoming hackathons
            - Register for events
            - Track your progress
            
            Happy Hacking!
            
            Best regards,
            The HackX Team
        """.trimIndent()

        sendEmail(userEmail, subject, message)
    }

    private suspend fun sendEmail(toEmail: String, subject: String, messageText: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Setting up email properties...")
                val properties = Properties().apply {
                    put("mail.smtp.host", HOST)
                    put("mail.smtp.port", PORT)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.ssl.trust", HOST)
                    put("mail.debug", "true")
                }

                Log.d(TAG, "Creating mail session...")
                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD)
                    }
                })

                Log.d(TAG, "Creating message...")
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(ADMIN_EMAIL, ADMIN_NAME))
                    addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    setSubject(subject)
                    setText(messageText)
                }

                Log.d(TAG, "Sending email...")
                Transport.send(message)
                Log.d(TAG, "Email sent successfully!")
            } catch (e: MessagingException) {
                Log.e(TAG, "Failed to send email", e)
                throw e
            }
        }
    }
} 