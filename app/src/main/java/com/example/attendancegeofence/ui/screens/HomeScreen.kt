package com.example.attendancegeofence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.attendancegeofence.data.models.Course
import com.example.attendancegeofence.data.models.Session
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    
    var userName by remember { mutableStateOf("User") }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var coursesMap by remember { mutableStateOf<Map<String, Course>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Fetch User Profile
            firestore.collection("users").document(uid).get().addOnSuccessListener {
                userName = it.getString("name") ?: "User"
            }
        }

        // Fetch Today's Sessions
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        firestore.collection("sessions")
            .whereGreaterThanOrEqualTo("startTime", com.google.firebase.Timestamp(startOfDay))
            .whereLessThanOrEqualTo("startTime", com.google.firebase.Timestamp(endOfDay))
            .get()
            .addOnSuccessListener { sessionDocs ->
                val fetchedSessions = sessionDocs.toObjects(Session::class.java)
                sessions = fetchedSessions

                // Fetch Unique Courses for these sessions
                val courseIds = fetchedSessions.map { it.courseId }.distinct()
                if (courseIds.isNotEmpty()) {
                    firestore.collection("courses")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), courseIds)
                        .get()
                        .addOnSuccessListener { courseDocs ->
                            coursesMap = courseDocs.toObjects(Course::class.java).associateBy { it.id }
                            isLoading = false
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                } else {
                    isLoading = false
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        containerColor = Color(0xFFF7FAFC),
        bottomBar = { HomeBottomNavigation(navController) }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002045))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeTopBar(userName)
                    Spacer(modifier = Modifier.height(40.dp))
                    HomeHeader()
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeSubHeader(sessions.size)
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    SectionHeader(
                        title = "Today's Classes",
                        actionText = "Full Schedule",
                        onActionClick = { navController.navigate("schedule") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (sessions.isEmpty()) {
                    item {
                        Text(
                            "No classes scheduled for today.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(sessions) { session ->
                        val course = coursesMap[session.courseId]
                        TodayClassCard(
                            title = course?.title ?: "Unknown Course",
                            time = formatTime(session.startTime.toDate()),
                            location = session.location,
                            icon = getIconForName(course?.iconName)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Recent Performance",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181C1E)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AttendanceWarningCard(
                        message = "Keep it up! Your average attendance is 94% across all registered modules."
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun formatTime(date: Date): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}

private fun getIconForName(name: String?): ImageVector {
    return when (name?.lowercase()) {
        "computer" -> Icons.Default.Computer
        "psychology" -> Icons.Default.Psychology
        "school" -> Icons.Default.School
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "calculate" -> Icons.Default.Calculate
        "balance" -> Icons.Default.Balance
        else -> Icons.Default.Book
    }
}

@Composable
fun HomeTopBar(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E3E5))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Good Morning",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF1A365D),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF43474E),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = Color(0xFF1A365D),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun HomeHeader() {
    Column {
        Text(
            text = "Your",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = Color(0xFF002045),
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
        Text(
            text = "Academic",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = Color(0xFF002045),
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
        Text(
            text = "Momentum",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = Color(0xFFA04100),
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
    }
}

@Composable
fun HomeSubHeader(classCount: Int) {
    Text(
        text = "You have $classCount classes scheduled for today. Access your full schedule to view details and mark attendance.",
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF74777F),
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        )
    )
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF181C1E)
            )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onActionClick() }
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF1A365D),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF1A365D),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TodayClassCard(title: String, time: String, location: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F4F6)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF1A365D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002045)
                    )
                )
                Text(
                    text = "$time • $location",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF74777F),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFC4C6CF),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun AttendanceWarningCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEBEEF0).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF1A365D),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A365D),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

@Composable
fun HomeBottomNavigation(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            HomeBottomNavItem(icon = Icons.Default.Home, label = "HOME", isSelected = true, onClick = {})
            HomeBottomNavItem(
                icon = Icons.Outlined.Schedule,
                label = "SCHEDULE",
                onClick = { navController.navigate("schedule") }
            )
            HomeBottomNavItem(
                icon = Icons.Outlined.LocationOn, 
                label = "CHECK-IN", 
                onClick = { navController.navigate("checkin") }
            )
            HomeBottomNavItem(
                icon = Icons.Outlined.History, 
                label = "HISTORY", 
                onClick = { navController.navigate("history") }
            )
        }
    }
}

@Composable
fun HomeBottomNavItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    if (isSelected) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFF1A365D),
                shadowElevation = 8.dp,
                onClick = onClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A365D),
                    letterSpacing = 0.5.sp
                )
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF43474E),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF43474E),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AttendanceGeoFenceTheme {
        HomeScreen(rememberNavController())
    }
}
