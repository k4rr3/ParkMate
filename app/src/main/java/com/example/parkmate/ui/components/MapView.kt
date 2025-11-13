package com.example.parkmate.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.parkmate.mock.ParkingSpot
import com.example.parkmate.utils.rememberDeviceLocation // <-- Importa la nueva función
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapView(
    parkingSpots: List<ParkingSpot>,
    hasLocationPermission: Boolean, // <-- CAMBIO 1: Recibe el estado del permiso.
    onSpotClick: (ParkingSpot) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // La lógica de permisos (popups, etc.) ya no está aquí, se gestiona desde fuera.
    Box(modifier = modifier.fillMaxSize()) {
        FullMapView(
            parkingSpots = parkingSpots,
            hasLocationPermission = hasLocationPermission, // <-- Pasa el estado hacia abajo.
            onCircleClick = onSpotClick,
            onMapClick = onMapClick
        )
    }
}

@Composable
private fun FullMapView(
    parkingSpots: List<ParkingSpot>,
    hasLocationPermission: Boolean, // <-- CAMBIO 2: Recibe el estado aquí también.
    onCircleClick: (ParkingSpot) -> Unit,
    onMapClick: () -> Unit
) {
    // --- LÓGICA DE POSICIÓN DE LA CÁMARA ---
    val lleidaDefault = LatLng(41.6175, 0.6200)

    // 1. Obtiene la ubicación del dispositivo (será null si no hay permiso o no se encuentra).
    val deviceLocation = rememberDeviceLocation(hasPermission = hasLocationPermission)

    // 2. Decide el punto de inicio: la ubicación del usuario si está disponible, si no, Lleida.
    val startLocation = deviceLocation ?: lleidaDefault

    // 3. Decide el nivel de zoom: más cercano (16f) si es la ubicación del usuario, más alejado (14f) si es la por defecto.
    val startZoom = if (deviceLocation != null) 16f else 14f

    // 4. Crea y recuerda el estado de la cámara con los valores decididos.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, startZoom)
    }

    // --- MAPA ---
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = hasLocationPermission // El botón "Mi Ubicación" solo aparece si hay permiso.
        ),
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission // El punto azul de "Mi Ubicación" solo se muestra si hay permiso.
        ),
        onMapClick = { onMapClick() }
    ) {
        parkingSpots.forEach { spot ->
            Circle(
                center = LatLng(spot.latitude, spot.longitude),
                radius = 50.0,
                fillColor = spot.color.copy(alpha = 0.2f),
                strokeWidth = 0f,
                tag = spot,
                clickable = true,
                onClick = {
                    onCircleClick(it.tag as ParkingSpot)
                }
            )
        }
    }
}
