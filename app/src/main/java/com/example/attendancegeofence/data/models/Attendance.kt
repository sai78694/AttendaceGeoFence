package com.example.attendancegeofence.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Attendance(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val sessionId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "PRESENT" // PRESENT, ABSENT
)
