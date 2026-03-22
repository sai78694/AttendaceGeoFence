package com.example.attendancegeofence.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Session(
    @DocumentId
    val id: String = "",
    val courseId: String = "",
    val instructor: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 50.0,
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val type: String = "LECTURE" // LECTURE, WORKSHOP, SEMINAR
)
