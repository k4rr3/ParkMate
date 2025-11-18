package com.example.parkmate.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.parkmate.mock.ParkingSpot
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

// This composable displays the map and handles location permissions.
@Composable
fun MapView(
    parkingSpots: List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit, // Callback for when a parking spot circle is clicked.
    onMapClick: () -> Unit,             // Callback for when the empty map area is clicked.
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }

    // Manages the runtime permission request for location.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            hasLocationPermission = context.hasLocationPermission()
        }
    )

    // Triggers the permission request when the composable first appears.
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Refreshes the permission status when the app resumes (e.g., returning from settings).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = context.hasLocationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Chooses what to display based on whether permission has been granted.
    Box(modifier = modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            FullMapView(
                parkingSpots = parkingSpots,
                onCircleClick = onSpotClick,
                onMapClick = onMapClick // Pass the map click callback down.
            )
        } else {
            // Shows a screen that prompts the user to open app settings.
            PermissionRequestScreen(
                onOpenSettings = { context.openAppSettings() }
            )
        }
    }
}

// A screen to ask the user for location permissions.
@Composable
private fun PermissionRequestScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Location permission is required to show your position on the map.\nPlease enable it in App Settings.",
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Open App Settings")
        }
    }
}

// The actual Google Map implementation.
@Composable
private fun FullMapView(
    parkingSpots: List<ParkingSpot>,
    onCircleClick: (ParkingSpot) -> Unit,
    onMapClick: () -> Unit
) {
    val lleida = LatLng(41.6175, 0.6200)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lleida, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true
        ),
        properties = MapProperties(isMyLocationEnabled = true),
        onMapClick = { onMapClick() } // Execute the callback when the map is clicked.
    ) {
        // Draws a circle on the map for each parking spot.
        parkingSpots.forEach { spot ->
            Circle(
                center = LatLng(spot.latitude, spot.longitude),
                radius = 50.0,
                fillColor = spot.color.copy(alpha = 0.2f),
                strokeWidth = 0f,
                tag = spot, // Attaches the spot data to the UI element.
                clickable = true,
                onClick = {
                    // Notifies the parent composable when a circle is clicked.
                    onCircleClick(it.tag as ParkingSpot)
                }
            )
        }
    }
}

// Checks if location permissions have been granted.
fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

// Opens the app's specific settings screen.
private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    startActivity(intent)
}
