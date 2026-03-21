package com.example.attendancegeofence.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.attendancegeofence.geofence.GeofenceManager
import com.example.attendancegeofence.ui.theme.AttendanceGeoFenceTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Coordinates for testing (e.g., Hall 4B)
// Chennai coordinates as placeholder
const val TARGET_LAT = 12.98416852946774
const val TARGET_LONG = 80.21770082334685
const val GEOFENCE_RADIUS_METERS = 50.0

@Composable
fun CheckInScreen(navController: NavController) {
    val context = LocalContext.current
    val geofenceManager = remember { GeofenceManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var distanceToTarget by remember { mutableStateOf<Double?>(null) }
    var isWithinRange by remember { mutableStateOf(false) }
    var foregroundPermissionsGranted by remember { mutableStateOf(false) }
    var backgroundPermissionGranted by remember { mutableStateOf(false) }

    // Launcher for background location permission (Android 10+)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundPermissionGranted = granted
    }

    // Launcher for foreground location permissions
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        foregroundPermissionsGranted = permissions.values.all { it }
        if (foregroundPermissionsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // After foreground is granted, ask for background
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            backgroundPermissionGranted = true
        }
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocation || !coarseLocation) {
            foregroundLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            foregroundPermissionsGranted = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundPermissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (!backgroundPermissionGranted) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                backgroundPermissionGranted = true
            }
        }
    }

    // Real-time location tracking
    LaunchedEffect(foregroundPermissionsGranted) {
        if (foregroundPermissionsGranted) {
            while (true) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val distance = calculateDistance(
                                location.latitude, location.longitude,
                                TARGET_LAT, TARGET_LONG
                            )
                            distanceToTarget = distance
                            isWithinRange = distance <= GEOFENCE_RADIUS_METERS
                        }
                    }
                delay(5000) // Update every 5 seconds
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7FAFC),
        bottomBar = { CheckInBottomNavigation(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            CheckInTopBar()

            Spacer(modifier = Modifier.height(24.dp))

            // "Current Session" Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFEBEEF0)
            ) {
                Text(
                    text = "CURRENT SESSION",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF1A365D)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CheckInHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Geofence Visualizer Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GeofenceVisualizer(isWithinRange)

                    if (!isWithinRange) {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutOfRangeWarning(distanceToTarget?.toInt() ?: 0)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            MetricsGrid(isWithinRange, distanceToTarget?.toInt() ?: 0)

            Spacer(modifier = Modifier.height(32.dp))
            MarkAttendanceButton(isWithinRange) {
                if (backgroundPermissionGranted) {
                    geofenceManager.addGeofence("hall_4b", TARGET_LAT, TARGET_LONG)
                    // TODO: Trigger success animation or API call
                } else {
                    // Fallback or warning if background permission is missing
                }
            }

            if (!isWithinRange) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Verification requires proximity and active GPS",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF74777F),
                        textAlign = TextAlign.Center
                    )
                )
            }

            if (isWithinRange) {
                Spacer(modifier = Modifier.height(24.dp))
                StatusMessage()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Haversine formula
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3
    val phi1 = lat1 * PI / 180
    val phi2 = lat2 * PI / 180
    val deltaPhi = (lat2 - lat1) * PI / 180
    val deltaLambda = (lon2 - lon1) * PI / 180
    val a = sin(deltaPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable
fun CheckInTopBar() {
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
            Column {
                Text(
                    text = "GOOD MORNING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF74777F),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Alex Johnson",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1A365D),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = Color(0xFF43474E),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CheckInHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Hall 4B",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
                color = Color(0xFF002045),
                letterSpacing = (-1).sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Advanced Macroeconomics • Section A",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF43474E),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun GeofenceVisualizer(isWithinRange: Boolean) {
    val accentColor = if (isWithinRange) Color(0xFF388E3C) else Color(0xFFBA1A1A)
    val bgColor = if (isWithinRange) Color(0xFF00BFA5) else Color(0xFFBA1A1A)

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            drawCircle(
                color = bgColor.copy(alpha = 0.05f),
                radius = size.minDimension / 2
            )
            drawCircle(
                color = bgColor.copy(alpha = 0.1f),
                radius = size.minDimension / 2.4f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Surface(
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.1f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isWithinRange) Icons.Default.VerifiedUser else Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (isWithinRange) "Geofence Active" else "Geofence Inactive",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002045)
        )
    )
    Text(
        text = if (isWithinRange) "WITHIN RANGE" else "OUT OF RANGE",
        style = MaterialTheme.typography.labelSmall.copy(
            color = accentColor,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    )
}

@Composable
fun OutOfRangeWarning(distance: Int) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFDAD6).copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFFFDAD6).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFBA1A1A),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "You are ${distance}m away from the classroom. Please move closer to enable check-in.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF410002),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
fun MarkAttendanceButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color(0xFF1A365D) else Color(0xFFEBEEF0),
            contentColor = if (isEnabled) Color.White else Color(0xFFC4C6CF)
        ),
        enabled = isEnabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEnabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Mark Attendance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            if (isEnabled) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MetricsGrid(isWithinRange: Boolean, distance: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            label = "TIME REMAINING",
            value = "08:42",
            iconColor = Color(0xFF1A365D)
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.PersonPinCircle,
            label = "DISTANCE",
            value = "${distance}m",
            iconColor = if (isWithinRange) Color(0xFFA04100) else Color(0xFFBA1A1A),
            valueColor = if (isWithinRange) Color(0xFF002045) else Color(0xFFBA1A1A)
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    valueColor: Color = Color(0xFF002045)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEBEEF0).copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF43474E),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = valueColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

@Composable
fun StatusMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Signal is strong. Your device is ready for check-in.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun CheckInBottomNavigation(navController: NavController) {
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
            CheckInBottomNavItem(icon = Icons.Outlined.Home, label = "HOME", onClick = {})
            CheckInBottomNavItem(
                icon = Icons.Outlined.Schedule,
                label = "SCHEDULE",
                onClick = { navController.navigate("schedule") }
            )
            CheckInBottomNavItem(
                icon = Icons.Default.LocationOn,
                label = "CHECK-IN",
                isSelected = true,
                onClick = {})
            CheckInBottomNavItem(icon = Icons.Outlined.History, label = "HISTORY", onClick = {})
        }
    }
}

@Composable
fun CheckInBottomNavItem(
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
fun CheckInScreenPreview() {
    AttendanceGeoFenceTheme {
        CheckInScreen(rememberNavController())
    }
}
