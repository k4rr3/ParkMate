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

// En: app/src/main/java/com/example/parkmate/utils/LocationUtils.kt
// Este composable obtiene la última ubicación conocida del usuario de forma segura.
@SuppressLint("MissingPermission")
@Composable
fun rememberDeviceLocation(hasPermission: Boolean): LatLng? {
    // El estado que almacenará la ubicación. Es nullable por si no se encuentra.
    var deviceLocation by remember { mutableStateOf<LatLng?>(LatLng(41.6081351,0.6230894)) }
    val context = LocalContext.current

    // Usamos LaunchedEffect para que esta lógica se ejecute solo cuando el permiso cambie.
    LaunchedEffect(hasPermission) {
        // Solo intentamos obtener la ubicación si tenemos el permiso.
        if (hasPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // Si la API de Google nos devuelve una ubicación, la actualizamos.
                if (location != null) {
                    deviceLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }
    // Devolvemos la ubicación encontrada.
    return deviceLocation
}