package com.example.parkmate.ui.components

import android.Manifest
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.parkmate.utils.hasLocationPermission // We will move the function here

/**
 * A class that manages location permission state and requests.
 * This makes the permission logic reusable and clean.
 */
class PermissionManager(
    var hasPermission: Boolean,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    // Public function to launch the permission request dialog.
    fun requestPermissions() {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

// A composable function that creates and remembers the PermissionManager.
@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // After the user responds to the dialog, we refresh the state.
        hasPermission = context.hasLocationPermission()
    }

    // This effect handles the case where the user comes back to the app
    // after changing permissions in the system settings.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // The manager is remembered across recompositions.
    return remember {
        PermissionManager(
            hasPermission = hasPermission,
            launcher = launcher
        )
    }.apply {
        this.hasPermission = hasPermission // Always keep the permission state updated.
    }
}
