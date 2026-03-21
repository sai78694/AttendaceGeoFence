package com.example.attendancegeofence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme

data class CalendarDate(
    val day: String,
    val date: String,
    val isActive: Boolean = false
)

data class ClassPeriod(
    val timeStart: String,
    val timeEnd: String,
    val title: String,
    val location: String,
    val instructor: String,
    val type: String, // LECTURE, ONGOING, WORKSHOP
    val isOngoing: Boolean = false
)

@Composable
fun ScheduleScreen(navController: NavController) {
    val dates = listOf(
        CalendarDate("MON", "14"),
        CalendarDate("TUE", "15", true),
        CalendarDate("WED", "16"),
        CalendarDate("THU", "17"),
        CalendarDate("FRI", "18")
    )

    val classes = listOf(
        ClassPeriod(
            "09:00", "10:30 AM",
            "Advanced Thermodynamics & Fluid Mechanics",
            "Hall 4B • Engineering Bldg",
            "Dr. Julian Vance",
            "LECTURE"
        ),
        ClassPeriod(
            "11:00", "12:30 PM",
            "Human-Computer Interaction Seminar",
            "Lab 202 • Innovation Hub",
            "Prof. Sarah Chen",
            "ONGOING",
            isOngoing = true
        ),
        ClassPeriod(
            "14:00", "16:00 PM",
            "Digital Signal Processing & Systems",
            "South Wing • Room 12",
            "Mr. Marcus Roe",
            "WORKSHOP"
        )
    )

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
                CalendarStrip(dates)
                Spacer(modifier = Modifier.height(40.dp))
                SectionTitle(title = "Today's Classes")
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(classes) { classItem ->
                ClassCard(classItem)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
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
                    .background(Color(0xFFE0E3E5)) // placeholder for student image
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
fun CalendarStrip(dates: List<CalendarDate>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(dates) { date ->
            DateItem(date)
        }
    }
}

@Composable
fun DateItem(date: CalendarDate) {
    if (date.isActive) {
        Column(
            modifier = Modifier
                .width(84.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF002045))
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
fun ClassCard(item: ClassPeriod) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box {
            if (item.isOngoing) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
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
                            text = item.timeStart,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isOngoing) Color(0xFFA04100) else Color(0xFF002045),
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = item.timeEnd,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF43474E),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (item.isOngoing) {
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
                    color = if (item.isOngoing) Color(0xFFFE6B00) else Color(0xFFF1F4F6),
                ) {
                    Text(
                        text = item.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isOngoing) Color.White else Color(0xFF43474E),
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181C1E),
                        fontSize = 24.sp,
                        lineHeight = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag(icon = Icons.Outlined.LocationOn, text = item.location)
                    InfoTag(icon = Icons.Outlined.Person, text = item.instructor)
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
            ScheduleBottomNavItem(icon = Icons.Outlined.Home, label = "HOME", onClick = {})
            ScheduleBottomNavItem(icon = Icons.Default.CalendarToday, label = "SCHEDULE", isSelected = true, onClick = {})
            ScheduleBottomNavItem(
                icon = Icons.Outlined.LocationOn, 
                label = "CHECK-IN", 
                onClick = { navController.navigate("checkin") }
            )
            ScheduleBottomNavItem(icon = Icons.Outlined.History, label = "HISTORY", onClick = {})
        }
    }
}

@Composable
fun ScheduleBottomNavItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
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
