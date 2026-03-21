package com.example.attendancegeofence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendancegeofence.ui.screens.CheckInScreen
import com.example.attendancegeofence.ui.screens.LoginScreen
import com.example.attendancegeofence.ui.screens.RegisterScreen
import com.example.attendancegeofence.ui.screens.ScheduleScreen
import com.example.attendancegeofence.ui.screens.WelcomeScreen
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendanceGeoFenceTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onSignUpClick = { navController.navigate("register") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginClick = { _, _ -> navController.navigate("schedule") },
                onSignUpClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterClick = { navController.navigate("schedule") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("schedule") {
            ScheduleScreen(navController = navController)
        }
        composable("checkin") {
            CheckInScreen(navController = navController)
        }
    }
}
