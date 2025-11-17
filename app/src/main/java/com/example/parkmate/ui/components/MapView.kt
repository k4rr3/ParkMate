package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.parkmate.data.models.Zone
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapView(
    zones: List<Zone>,
    hasLocationPermission: Boolean,
    onZoneClick: (Zone) -> Unit,
    onMapClick: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.6168, 0.6226), 14f)
    }

    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(MapProperties(isMyLocationEnabled = hasLocationPermission))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        onMapClick = { onMapClick() }
    ) {
        zones.forEach { zone ->
            if (zone.vector.size >= 3) {
                Polygon(
                    points = zone.vector.map { LatLng(it.latitude, it.longitude) },
                    clickable = true,
                    onClick = { onZoneClick(zone) },
                    strokeColor = getZoneStrokeColor(zone.name),
                    strokeWidth = 6f,
                    fillColor = getZoneFillColor(zone.name)
                )
            }
        }
    }
}

private fun getZoneFillColor(zoneName: String): Color {
    return when {
        zoneName.contains("Azul", ignoreCase = true) -> Color(0x6687CEEB)
        zoneName.contains("Verde", ignoreCase = true) -> Color(0x6698FB98)
        zoneName.contains("Carga", ignoreCase = true) -> Color(0x66FFD700)
        zoneName.contains("Gratuito", ignoreCase = true) -> Color(0x66B0B0B0)
        else -> Color.Gray.copy(alpha = 0.4f)
    }
}

private fun getZoneStrokeColor(zoneName: String): Color {
    return when {
        zoneName.contains("Azul", ignoreCase = true) -> Color(0xFF0066CC)
        zoneName.contains("Verde", ignoreCase = true) -> Color(0xFF228B22)
        zoneName.contains("Carga", ignoreCase = true) -> Color(0xFFFFA500)
        zoneName.contains("Gratuito", ignoreCase = true) -> Color(0xFF696969)
        else -> Color.DarkGray
    }
}
