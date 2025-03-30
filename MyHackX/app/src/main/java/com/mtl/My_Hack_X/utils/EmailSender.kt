package com.mtl.My_Hack_X.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mtl.My_Hack_X.data.model.Hackathon
import java.util.*

object EmailSender {
    private const val ADMIN_EMAIL = "dhruvil2361@gmail.com"

    fun sendNewUserNotification(context: Context, userEmail: String, userName: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))  // Send to admin
            putExtra(Intent.EXTRA_SUBJECT, "New User Registration")
            putExtra(Intent.EXTRA_TEXT, """
                A new user has registered in the HackX app.
                
                User Details:
                - Email: $userEmail
                - Name: $userName
                
                This is an automated notification.
            """.trimIndent())
        }
        context.startActivity(Intent.createChooser(intent, "Sending registration notification..."))
    }

    fun sendRegistrationEmail(context: Context, hackathon: Hackathon, userEmail: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmail))  // Send to user
            putExtra(Intent.EXTRA_SUBJECT, "Registration Confirmation: ${hackathon.title}")
            putExtra(Intent.EXTRA_TEXT, """
                Dear Participant,
                
                Your registration for ${hackathon.title} has been confirmed.
                
                Event Details:
                - Start Date: ${formatDate(hackathon.startDate)}
                - End Date: ${formatDate(hackathon.endDate)}
                - Location: ${hackathon.location}
                - Prize Pool: ${hackathon.prizePool}
                
                Please keep this email for your records.
                
                Best regards,
                HackX Team
            """.trimIndent())
        }
        context.startActivity(intent)
    }

    fun sendWelcomeEmail(context: Context, email: String, name: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Welcome to HackX!")
            putExtra(Intent.EXTRA_TEXT, """
                Hi $name,
                
                Welcome to HackX! Your account has been successfully created.
                
                You can now:
                - Browse upcoming hackathons
                - Register for events
                - Track your registrations
                
                Happy Hacking!
                
                Best regards,
                The HackX Team
            """.trimIndent())
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }

    fun sendRegistrationNotificationToAdmin(
        context: Context,
        hackathon: Hackathon,
        userName: String,
        userEmail: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "New Registration: ${hackathon.title}")
            putExtra(Intent.EXTRA_TEXT, """
                New Registration Details:
                
                Event: ${hackathon.title}
                Participant Name: $userName
                Email: $userEmail
                
                Registration Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date())}
            """.trimIndent())
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }
    
    private fun formatDate(date: Date): String {
        return java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
} 