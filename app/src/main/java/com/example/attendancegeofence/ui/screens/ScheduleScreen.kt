package com.example.attendancegeofence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDate(
    val day: String,
    val date: String,
    val isActive: Boolean = false,
    val fullDate: Date
)

@Composable
fun ScheduleScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var coursesMap by remember { mutableStateOf<Map<String, Course>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Generate dynamic dates for the calendar strip (Current week)
    val dates = remember {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        (0..4).map {
            val date = calendar.time
            val isToday = calendar.get(Calendar.DAY_OF_YEAR) == currentDay
            val item = CalendarDate(
                day = SimpleDateFormat("EEE", Locale.getDefault()).format(date).uppercase(),
                date = SimpleDateFormat("dd", Locale.getDefault()).format(date),
                isActive = isToday,
                fullDate = date
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            item
        }
    }

    LaunchedEffect(selectedDate) {
        isLoading = true
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
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
                    .sortedBy { it.startTime }
                sessions = fetchedSessions

                val courseIds = fetchedSessions.map { it.courseId }.distinct()
                if (courseIds.isNotEmpty()) {
                    firestore.collection("courses")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), courseIds)
                        .get()
                        .addOnSuccessListener { courseDocs ->
                            coursesMap =
                                courseDocs.toObjects(Course::class.java).associateBy { it.id }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                } else {
                    isLoading = false
                }
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        containerColor = Color(0xFFF7FAFC),
        bottomBar = { ScheduleBottomNavigation(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ScheduleTopBar()
                Spacer(modifier = Modifier.height(32.dp))
                ScheduleHeader()
                Spacer(modifier = Modifier.height(32.dp))
                CalendarStrip(dates) { date ->
                    selectedDate = date
                }
                Spacer(modifier = Modifier.height(40.dp))
                SectionTitle(title = "Class Schedule")
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF002045))
                    }
                }
            } else if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "No classes scheduled for this day.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                items(sessions) { session ->
                    val course = coursesMap[session.courseId]
                    val isOngoing = isSessionOngoing(session)
                    ClassCard(
                        timeStart = formatTime(session.startTime.toDate()),
                        timeEnd = formatTime(session.endTime.toDate()),
                        title = course?.title ?: "Unknown Course",
                        location = session.location,
                        instructor = session.instructor,
                        type = session.type,
                        isOngoing = isOngoing
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun formatTime(date: Date): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}

private fun isSessionOngoing(session: Session): Boolean {
    val now = Date()
    return now.after(session.startTime.toDate()) && now.before(session.endTime.toDate())
}

@Composable
fun ScheduleTopBar() {
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
                text = "Good Morning",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1A365D),
                    fontWeight = FontWeight.Medium
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
fun ScheduleHeader() {
    Column {
        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
                color = Color(0xFF002045),
                letterSpacing = (-1).sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Autumn Semester 2024 • Engineering Faculty",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF43474E),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun CalendarStrip(dates: List<CalendarDate>, onDateSelected: (Date) -> Unit) {
    var activeDate by remember {
        mutableStateOf(
            dates.find { it.isActive }?.fullDate ?: dates[0].fullDate
        )
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(dates) { date ->
            DateItem(date, isActive = date.fullDate == activeDate) {
                activeDate = date.fullDate
                onDateSelected(date.fullDate)
            }
        }
    }
}

@Composable
fun DateItem(date: CalendarDate, isActive: Boolean, onClick: () -> Unit) {
    if (isActive) {
        Column(
            modifier = Modifier
                .width(84.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF002045))
                .clickable { onClick() }
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.day,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = date.date,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFE6B00))
            )
        }
    } else {
        Column(
            modifier = Modifier
                .width(72.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F4F6))
                .clickable { onClick() }
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.day,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF43474E),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = date.date,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF181C1E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF002045)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFA04100))
        )
    }
}

@Composable
fun ClassCard(
    timeStart: String,
    timeEnd: String,
    title: String,
    location: String,
    instructor: String,
    type: String,
    isOngoing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box {
            if (isOngoing) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .width(4.dp)
                        .background(Color(0xFFFE6B00))
                        .align(Alignment.CenterStart)
                )
            }

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = timeStart,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOngoing) Color(0xFFA04100) else Color(0xFF002045),
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = timeEnd,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF43474E),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (isOngoing) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Ongoing",
                            tint = Color(0xFFFE6B00),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFFC4C6CF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isOngoing) Color(0xFFFE6B00) else Color(0xFFF1F4F6),
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOngoing) Color.White else Color(0xFF43474E),
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181C1E),
                        fontSize = 24.sp,
                        lineHeight = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag(icon = Icons.Outlined.LocationOn, text = location)
                    InfoTag(icon = Icons.Outlined.Person, text = instructor)
                }
            }
        }
    }
}

@Composable
fun InfoTag(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F4F6)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF43474E),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF181C1E),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun ScheduleBottomNavigation(navController: NavController) {
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
            ScheduleBottomNavItem(
                icon = Icons.Outlined.Home,
                label = "HOME",
                onClick = { navController.navigate("home") }
            )
            ScheduleBottomNavItem(
                icon = Icons.Default.CalendarToday,
                label = "SCHEDULE",
                isSelected = true,
                onClick = {})
            ScheduleBottomNavItem(
                icon = Icons.Outlined.LocationOn,
                label = "CHECK-IN",
                onClick = { navController.navigate("checkin") }
            )
            ScheduleBottomNavItem(
                icon = Icons.Outlined.History,
                label = "HISTORY",
                onClick = { navController.navigate("history") }
            )
        }
    }
}

@Composable
fun ScheduleBottomNavItem(
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
fun ScheduleScreenPreview() {
    AttendanceGeoFenceTheme {
        ScheduleScreen(rememberNavController())
    }
}
