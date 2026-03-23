package com.example.attendancegeofence.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceReceiver", "Error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        for (geofence in triggeringGeofences) {
            val sessionId = geofence.requestId

            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("GeofenceReceiver", "GEOFENCE_TRANSITION_EXIT for session: $sessionId")
                    // If they exit before the session ends, mark them as ABSENT
                    firestore.collection("sessions").document(sessionId).get()
                        .addOnSuccessListener { document ->
                            val endTime = document.getTimestamp("endTime")?.toDate()?.time ?: 0
                            val now = System.currentTimeMillis()
                            
                            if (now < endTime) {
                                // User left early, update status to ABSENT
                                firestore.collection("attendance")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("sessionId", sessionId)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        for (doc in querySnapshot.documents) {
                                            doc.reference.update("status", "ABSENT")
                                        }
                                    }
                            }
                        }
                }
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d("GeofenceReceiver", "GEOFENCE_TRANSITION_ENTER for session: $sessionId")
                }
            }
        }
    }
}
