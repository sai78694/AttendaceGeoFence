package com.example.attendancegeofence.data.models

import com.google.firebase.firestore.DocumentId

data class Course(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val code: String = "",
    val iconName: String = "school",
    val colorHex: String = "#002045"
)
