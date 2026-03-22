package com.example.attendancegeofence.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.attendancegeofence.data.models.Attendance
import com.example.attendancegeofence.data.models.Course
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class CourseAttendance(
    val code: String,
    val title: String,
    val totalSessions: Int,
    val attendedSessions: Int,
    val icon: ImageVector,
    val progressColor: Color,
    val recentSessions: List<SessionRecord>
)

data class SessionRecord(
    val date: String,
    val isPresent: Boolean
)

@Composable
fun HistoryScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    
    var attendanceData by remember { mutableStateOf<List<CourseAttendance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var overallRate by remember { mutableStateOf(0f) }
    var totalAttended by remember { mutableStateOf(0) }
    var totalExpected by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        Log.d("HistoryScreen", "Current UID: $uid")
        if (uid == null) {
            isLoading = false
            return@LaunchedEffect
        }

        // Fetch all attendance for the user
        // Note: orderBy with whereEqualTo requires a composite index.
        // If it fails, check Logcat for a link to create the index.
        firestore.collection("attendance")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { attendanceDocs ->
                Log.d("HistoryScreen", "Fetched ${attendanceDocs.size()} attendance records")
                val rawAttendance = attendanceDocs.toObjects(Attendance::class.java)
                
                if (rawAttendance.isEmpty()) {
                    isLoading = false
                    return@addOnSuccessListener
                }

                // Sort manually if index is not yet available, or just keep it as is
                val sortedAttendance = rawAttendance.sortedByDescending { it.timestamp }

                val courseIds = sortedAttendance.map { it.courseId }.distinct()
                Log.d("HistoryScreen", "Course IDs found: $courseIds")
                
                // Fetch Course details
                firestore.collection("courses")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), courseIds)
                    .get()
                    .addOnSuccessListener { courseDocs ->
                        Log.d("HistoryScreen", "Fetched ${courseDocs.size()} course records")
                        val coursesMap = courseDocs.toObjects(Course::class.java).associateBy { it.id }
                        
                        // Process data by course
                        val groupedByCourse = sortedAttendance.groupBy { it.courseId }
                        
                        val processedData = groupedByCourse.map { (courseId, records) ->
                            val course = coursesMap[courseId]
                            val attendedCount = records.count { it.status == "PRESENT" }
                            val expectedCount = records.size 

                            CourseAttendance(
                                code = course?.code ?: "N/A",
                                title = course?.title ?: "Unknown Course",
                                totalSessions = expectedCount,
                                attendedSessions = attendedCount,
                                icon = getIconForName(course?.iconName),
                                progressColor = parseColor(course?.colorHex),
                                recentSessions = records.take(3).map {
                                    SessionRecord(
                                        date = formatDate(it.timestamp.toDate()),
                                        isPresent = it.status == "PRESENT"
                                    )
                                }
                            )
                        }

                        attendanceData = processedData
                        totalAttended = sortedAttendance.count { it.status == "PRESENT" }
                        totalExpected = sortedAttendance.size
                        overallRate = if (totalExpected > 0) (totalAttended.toFloat() / totalExpected.toFloat()) * 100 else 0f
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("HistoryScreen", "Failed to fetch courses", e)
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("HistoryScreen", "Failed to fetch attendance", e)
                isLoading = false
            }
    }

    Scaffold(
        containerColor = Color(0xFFF7FAFC),
        bottomBar = { HistoryBottomNavigation(navController) }
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
                    HistoryTopBar()
                    Spacer(modifier = Modifier.height(32.dp))
                    HistoryHeader()
                    Spacer(modifier = Modifier.height(32.dp))
                    OverviewMetrics(overallRate, totalAttended, totalExpected)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                if (attendanceData.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No attendance records found.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(attendanceData) { course ->
                        CourseAttendanceCard(course)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
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

private fun parseColor(hex: String?): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex ?: "#002045"))
    } catch (e: Exception) {
        Color(0xFF002045)
    }
}

@Composable
fun HistoryTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E3E5))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Attendance History",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A365D),
                    fontWeight = FontWeight.Bold
                )
            )
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
fun HistoryHeader() {
    Column {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        Text(
            text = "Academic Year $year-${(year + 1) % 100}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF43474E),
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = "Attendance",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = Color(0xFF002045),
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
        Text(
            text = "Overview",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = Color(0xFF86A0CD),
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
    }
}

@Composable
fun OverviewMetrics(rate: Float, attended: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OverviewMetricCard(
            icon = Icons.Default.BarChart,
            label = "OVERALL RATE",
            value = String.format(Locale.getDefault(), "%.1f%%", rate),
            iconContainerColor = Color(0xFFA04100).copy(alpha = 0.1f),
            iconColor = Color(0xFFA04100)
        )
        OverviewMetricCard(
            icon = Icons.Default.CalendarToday,
            label = "SESSIONS",
            value = "$attended/$total",
            iconContainerColor = Color(0xFF1A365D).copy(alpha = 0.1f),
            iconColor = Color(0xFF1A365D)
        )
    }
}

@Composable
fun OverviewMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    iconContainerColor: Color,
    iconColor: Color
) {
    Surface(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconContainerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF74777F),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF002045),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CourseAttendanceCard(course: CourseAttendance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F4F6)
                    ) {
                        Text(
                            text = course.code,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A365D)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002045),
                            lineHeight = 28.sp
                        )
                    )
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFF1F4F6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = course.icon,
                            contentDescription = null,
                            tint = Color(0xFF1A365D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Attendance Progress",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181C1E)
                    )
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${course.attendedSessions}/${course.totalSessions}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF002045)
                        )
                    )
                    Text(
                        text = " sessions",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF43474E),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { if (course.totalSessions > 0) course.attendedSessions.toFloat() / course.totalSessions.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = course.progressColor,
                trackColor = Color(0xFFE0E3E5)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "RECENT SESSIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF74777F),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                course.recentSessions.forEach { session ->
                    SessionItem(session)
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: SessionRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7FAFC)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (session.isPresent) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (session.isPresent) Color(0xFF388E3C) else Color(0xFFBA1A1A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF181C1E),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Text(
                text = if (session.isPresent) "PRESENT" else "ABSENT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (session.isPresent) Color(0xFF74777F) else Color(0xFFBA1A1A),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun HistoryBottomNavigation(navController: NavController) {
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
            HistoryBottomNavItem(icon = Icons.Outlined.Home, label = "HOME", onClick = { navController.navigate("home") })
            HistoryBottomNavItem(
                icon = Icons.Outlined.Schedule,
                label = "SCHEDULE",
                onClick = { navController.navigate("schedule") }
            )
            HistoryBottomNavItem(
                icon = Icons.Outlined.LocationOn, 
                label = "CHECK-IN", 
                onClick = { navController.navigate("checkin") }
            )
            HistoryBottomNavItem(
                icon = Icons.Default.History, 
                label = "HISTORY", 
                isSelected = true, 
                onClick = {}
            )
        }
    }
}

@Composable
fun HistoryBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
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
fun HistoryScreenPreview() {
    AttendanceGeoFenceTheme {
        HistoryScreen(rememberNavController())
    }
}
