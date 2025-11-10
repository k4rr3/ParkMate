package com.example.parkmate.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@SuppressLint("MissingPermission")
@Composable
fun rememberDeviceLocation(hasPermission: Boolean): LatLng? {
    var deviceLocation by remember { mutableStateOf<LatLng?>(LatLng(41.6081351,0.6230894)) }
    val context = LocalContext.current

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    deviceLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }
    return deviceLocation
}