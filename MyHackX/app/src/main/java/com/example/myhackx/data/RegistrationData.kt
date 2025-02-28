package com.example.myhackx.data

data class RegistrationData(
    val userId: String,
    val hackathonName: String,
    val name: String,
    val mobileNumber: String,
    val college: String,
    val currentEducation: String,
    val isGraduate: Boolean,
    val field: String,
    val additionalInfo: String = "",
    val registrationDate: Long = System.currentTimeMillis()
) 