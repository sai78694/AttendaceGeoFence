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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme

data class TodayClass(
    val title: String,
    val time: String,
    val location: String,
    val icon: ImageVector
)

data class CourseBrief(
    val code: String,
    val percentage: String,
    val color: Color
)

@Composable
fun HomeScreen(navController: NavController) {
    val todayClasses = listOf(
        TodayClass("Computer Science", "10:00 AM", "Room 402", Icons.Default.Computer),
        TodayClass("Psychology", "02:00 PM", "Auditorium B", Icons.Default.Psychology)
    )

    val courseAttendance = listOf(
        CourseBrief("CS 101", "98%", Color(0xFF002045)),
        CourseBrief("PSY 204", "92%", Color(0xFF002045))
    )

    Scaffold(
        containerColor = Color(0xFFF7FAFC),
        bottomBar = { HomeBottomNavigation(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomeTopBar()
                Spacer(modifier = Modifier.height(40.dp))
                HomeHeader()
                Spacer(modifier = Modifier.height(16.dp))
                HomeSubHeader()
                Spacer(modifier = Modifier.height(40.dp))
                
                SectionHeader(
                    title = "Today's Classes",
                    actionText = "Full Schedule",
                    onActionClick = { navController.navigate("schedule") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(todayClasses) { item ->
                TodayClassCard(item)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Course Attendance",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181C1E)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    courseAttendance.forEach { brief ->
                        AttendanceBriefCard(brief, modifier = Modifier.weight(1f))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AttendanceWarningCard(
                    message = "Math 301 is approaching the minimum 80% threshold."
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HomeTopBar() {
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
                    text = "Alex Rivers",
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
                color = Color(0xFFA04100), // Secondary color
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
        )
    }
}

@Composable
fun HomeSubHeader() {
    Text(
        text = "You have 2 classes scheduled for today and your attendance rate is currently exceeding your semester goal.",
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
fun TodayClassCard(item: TodayClass) {
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
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color(0xFF1A365D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002045)
                    )
                )
                Text(
                    text = "${item.time} • ${item.location}",
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
fun AttendanceBriefCard(brief: CourseBrief, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFEBEEF0).copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = brief.code,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF43474E),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = brief.percentage,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF002045),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E3E5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // placeholder progress
                        .fillMaxHeight()
                        .background(brief.color)
                )
            }
        }
    }
}

@Composable
fun AttendanceWarningCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFDE7D9) // Light orange themed background
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFA04100),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF572000),
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
fun HomeBottomNavItem(
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
fun HomeScreenPreview() {
    AttendanceGeoFenceTheme {
        HomeScreen(rememberNavController())
    }
}
