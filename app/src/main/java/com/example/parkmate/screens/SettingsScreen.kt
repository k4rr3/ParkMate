package com.example.parkmate.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.parkmate.R
import com.example.parkmate.ui.theme.Blue
import com.example.parkmate.ui.theme.LightBlue
import com.example.parkmate.ui.theme.ThemeViewModel

// --- PERMISSION HANDLING LOGIC ---

/**
 * Manages the state and request for a single system permission.
 * @param permission The specific permission string, e.g., Manifest.permission.POST_NOTIFICATIONS.
 * @param hasPermission The current state of the permission.
 * @param launcher The ActivityResultLauncher to request the permission.
 */
class PermissionManager(
    val permission: String,
    var hasPermission: Boolean,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    // Launches the system dialog to request the permission.
    fun requestPermission() {
        launcher.launch(permission)
    }
}

/**
 * A composable factory to create and remember a [PermissionManager] for a specific permission.
 * It automatically handles state updates from the permission dialog and app lifecycle events.
 * @param permission The permission to manage.
 * @param onPermissionResult An optional callback triggered with the result of a permission request.
 * @return An instance of [PermissionManager].
 */
@Composable
fun rememberPermissionManager(
    permission: String,
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionManager {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasPermission(permission)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            onPermissionResult(isGranted)
        }
    )

    // Observes the app's lifecycle to refresh permission status on RESUME.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, permission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasPermission(permission)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Creates and remembers the PermissionManager instance.
    return remember(permission) {
        PermissionManager(
            permission = permission,
            hasPermission = hasPermission,
            launcher = launcher
        )
    }.apply {
        // Ensures the manager's state is always up-to-date.
        this.hasPermission = hasPermission
    }
}

/**
 * Extension function to check if a specific permission is granted for the current context.
 * Includes a compatibility check for notification permissions on older Android versions.
 */
fun Context.hasPermission(permission: String): Boolean {
    // For SDK < 33, POST_NOTIFICATIONS is implicitly granted.
    if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

// --- END OF PERMISSION LOGIC ---

// Navigates the user to the app's details screen in the system settings.
private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel
) {
    // Create and remember a manager for each required permission.
    val locationPermissionManager = rememberPermissionManager(Manifest.permission.ACCESS_FINE_LOCATION)
    val notificationPermissionManager = rememberPermissionManager(Manifest.permission.POST_NOTIFICATIONS)

    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = stringResource(R.string.app_settings))

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.Language),
                    subtitle = stringResource(R.string.English),
                    hasArrow = true,
                    onClick = { /* Handle language change */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                // Location Services toggle.
                SettingsItemWithSwitch(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.location_services),
                    subtitle = if (locationPermissionManager.hasPermission) stringResource(R.string.Always) else "Disabled",
                    checked = locationPermissionManager.hasPermission,
                    onCheckedChange = {
                        if (locationPermissionManager.hasPermission) {
                            // If permission is already granted, navigate to system settings.
                            openAppSettings(context)
                        } else {
                            // If permission is not granted, request it.
                            locationPermissionManager.requestPermission()
                        }
                    }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                // Notifications toggle.
                SettingsItemWithSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = if (notificationPermissionManager.hasPermission) "Enabled" else "Disabled",
                    checked = notificationPermissionManager.hasPermission,
                    onCheckedChange = {
                        if (notificationPermissionManager.hasPermission) {
                            openAppSettings(context)
                        } else {
                            notificationPermissionManager.requestPermission()
                        }
                    }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                // Dark Mode toggle.
                SettingsItemWithSwitch(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.dark_mode),
                    subtitle = stringResource(R.string.system),
                    checked = themeViewModel.getDarkMode(),
                    onCheckedChange = {  themeViewModel.toggleTheme() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Privacy & Security")

            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Shield,
                    title = "Data Sharing",
                    subtitle = "Limited sharing",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "App Permissions",
                    subtitle = "Manage access",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "FAQ, Contact us",
                    hasArrow = true,
                    onClick = { /* Handle click */ }
                )

                Divider(modifier = Modifier.padding(start = 72.dp))

                SettingsItem(
                    icon = Icons.Default.Info,
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


// --- UI COMPONENT COMPOSABLES ---

// Displays a title for a section of settings.
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color =  MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

// A container Card for a group of settings items.
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            content()
        }
    }
}

// A standard settings row with an icon, title, subtitle, and an optional navigation arrow.
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    hasArrow: Boolean = false,
    onClick: () -> Unit = {},
    titleColor: Color =  MaterialTheme.colorScheme.onBackground
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
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Blue,
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
                    color =  MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (hasArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Arrow",
                tint =  MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// A settings row that includes a Switch for toggling a state.
@Composable
fun SettingsItemWithSwitch(
    icon: ImageVector,
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
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Blue,
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
                    color =  MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor =  MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
