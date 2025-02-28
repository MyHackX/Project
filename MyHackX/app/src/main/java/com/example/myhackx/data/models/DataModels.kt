package com.example.myhackx.data.models

import com.example.myhackx.data.models.HackathonEvent

data class Registration(
    val userId: String,
    val hackathonName: String,
    val registrationDate: Long = System.currentTimeMillis()
) 