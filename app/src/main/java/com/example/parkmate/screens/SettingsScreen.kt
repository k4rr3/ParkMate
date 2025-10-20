package com.example.parkmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Settings Section
            SectionHeader(title = "App Settings")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconBgColor = LightBlue,
                    iconColor = Blue,
                    title = "Language",
                    subtitle = "English",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItemWithSwitch(
                    icon = Icons.Default.Notifications,
                    iconBgColor = LightOrange,
                    iconColor = Orange,
                    title = "Notifications",
                    subtitle = "Push, Email",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItemWithSwitch(
                    icon = Icons.Default.LocationOn,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "Location Services",
                    subtitle = "Always",
                    checked = locationEnabled,
                    onCheckedChange = { locationEnabled = it }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItemWithSwitch(
                    icon = Icons.Default.DarkMode,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "Dark Mode",
                    subtitle = "System",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Security Section
            SectionHeader(title = "Privacy & Security")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Shield,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "Data Sharing",
                    subtitle = "Limited sharing",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Security,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "App Permissions",
                    subtitle = "Manage access",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Help,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "Help & Support",
                    subtitle = "FAQ, Contact us",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Info,
                    iconBgColor = LightGreen,
                    iconColor = Green,
                    title = "About",
                    subtitle = "Version 2.4.1",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String?,
    hasArrow: Boolean = false,
    onClick: () -> Unit = {},
    titleColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        if (hasArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Arrow",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsItemWithSwitch(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
