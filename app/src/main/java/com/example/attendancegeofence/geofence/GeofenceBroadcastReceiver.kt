package com.example.attendancegeofence.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

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

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceReceiver", "GEOFENCE_TRANSITION_ENTER triggered")
                // TODO: Update app state or send notification to mark attendance
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "GEOFENCE_TRANSITION_EXIT triggered")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d("GeofenceReceiver", "GEOFENCE_TRANSITION_DWELL triggered")
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown transition: $geofenceTransition")
            }
        }
    }
}
