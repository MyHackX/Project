package com.example.myhackx.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.myhackx.data.models.HackathonEvent
import com.example.myhackx.data.RegistrationData
import java.util.*

object EmailSender {
    private const val ADMIN_EMAIL = "mtlmistry123@gmail.com"

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

    fun sendRegistrationEmail(context: Context, hackathon: HackathonEvent, userEmail: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmail))  // Send to user
            putExtra(Intent.EXTRA_SUBJECT, "Registration Confirmation: ${hackathon.name}")
            putExtra(Intent.EXTRA_TEXT, """
                Dear Participant,
                
                Your registration for ${hackathon.name} has been confirmed.
                
                Event Details:
                - Start Date: ${hackathon.startDate}
                - End Date: ${hackathon.endDate}
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
        registration: RegistrationData,
        hackathon: HackathonEvent
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "New Registration: ${hackathon.name}")
            putExtra(Intent.EXTRA_TEXT, """
                New Registration Details:
                
                Event: ${hackathon.name}
                Participant Name: ${registration.name}
                Mobile: ${registration.mobileNumber}
                College: ${registration.college}
                Education: ${registration.currentEducation}
                Graduate: ${if (registration.isGraduate) "Yes" else "No"}
                Field: ${registration.field}
                Additional Info: ${registration.additionalInfo}
                
                Registration Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(registration.registrationDate))}
            """.trimIndent())
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }
} 