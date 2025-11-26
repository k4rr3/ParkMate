package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.parkmate.data.models.InterestPoint // <-- NUEVA IMPORTACIÓN
import com.example.parkmate.data.models.Zone
import com.example.parkmate.ui.theme.Purple
import com.google.android.gms.maps.model.BitmapDescriptorFactory // <-- NUEVA IMPORTACIÓN
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapView(
    cameraPositionState: CameraPositionState,
    zones: List<Zone>,
    gasStations: List<InterestPoint>, // <-- NUEVO PARÁMETRO
    hasLocationPermission: Boolean,
    onZoneClick: (Zone) -> Unit,
    onGasStationClick: (InterestPoint) -> Unit, // <-- NUEVO CALLBACK
    onMapClick: () -> Unit
) {
    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(MapProperties(isMyLocationEnabled = hasLocationPermission))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        onMapClick = { onMapClick() }
    ) {
        // Lógica de Zonas (sin cambios)
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

        // NUEVA LÓGICA para dibujar los marcadores de gasolineras
        gasStations.forEach { station ->
            Marker(
                state = MarkerState(position = LatLng(station.location.latitude, station.location.longitude)),
                title = station.name,
                snippet = "Gasolinera",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), // Icono rojo
                onClick = {
                    onGasStationClick(station) // Llama al nuevo callback
                    true // Indica que el clic ha sido gestionado
                }
            )
        }
    }
}

// Funciones de color (sin cambios, excepto el color del borde que tenías mal)
private fun getZoneFillColor(zoneName: String): Color {
    return when {
        zoneName.contains("Azul", ignoreCase = true) -> Color(0x6687CEEB)
        zoneName.contains("Verde", ignoreCase = true) -> Color(0x6698FB98)
        zoneName.contains("Carga", ignoreCase = true) -> Color(0x66FFD700)
        zoneName.contains("Gratuito", ignoreCase = true) -> Color(0x66B0B0B0)
        else -> Purple.copy(alpha = 0.4f)
    }
}

private fun getZoneStrokeColor(zoneName: String): Color {
    return when {
        zoneName.contains("Azul", ignoreCase = true) -> Color(0xFF0066CC)
        zoneName.contains("Verde", ignoreCase = true) -> Color(0xFF228B22)
        zoneName.contains("Carga", ignoreCase = true) -> Color(0xFFFFA500)
        zoneName.contains("Gratuito", ignoreCase = true) -> Color(0xFF696969)
        else -> Purple // El borde debe ser sólido, no transparente. Corregido.
    }
}
