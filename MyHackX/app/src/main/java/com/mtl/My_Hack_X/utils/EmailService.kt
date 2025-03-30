package com.mtl.My_Hack_X.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService private constructor() {
    companion object {
        private const val TAG = "EmailService"
        private var instance: EmailService? = null
        
        // Email credentials
        private const val EMAIL_ADDRESS = "your-email@gmail.com"
        private const val EMAIL_PASSWORD = "your-app-specific-password"
        
        fun getInstance(): EmailService {
            return instance ?: synchronized(this) {
                instance ?: EmailService().also { instance = it }
            }
        }
    }

    private val emailProperties = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }

    private val session = Session.getInstance(emailProperties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(EMAIL_ADDRESS, EMAIL_PASSWORD)
        }
    })

    suspend fun sendWelcomeEmailToUser(recipientEmail: String, userName: String) {
        withContext(Dispatchers.IO) {
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL_ADDRESS))
                    addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                    subject = "Welcome to MyHackX!"
                    setText("""
                        Dear $userName,
                        
                        Welcome to MyHackX! We're excited to have you join our community of innovators and creators.
                        
                        Best regards,
                        The MyHackX Team
                    """.trimIndent())
                }
                
                Transport.send(message)
                Log.d(TAG, "Welcome email sent successfully to $recipientEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending welcome email: ${e.message}", e)
                throw e
            }
        }
    }
} 