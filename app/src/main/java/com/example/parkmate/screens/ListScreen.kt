package com.example.parkmate.ui

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parkmate.screens.rememberPermissionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

// Definimos un objeto de datos para nuestras categorías
data class ListCategory(
    val title: String,
    val icon: ImageVector,
    val destinationRoute: String
)

@Composable
fun ListScreen(
    onCategoryClick: (route: String, userLocation: LatLng?) -> Unit
) {
    val categories = listOf(
        ListCategory("Zonas de Aparcamiento", Icons.Default.LocalParking, "category_detail/zones"),
        ListCategory("Gasolineras", Icons.Default.LocalGasStation, "category_detail/gas_stations")
    )

    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val locationPermissionManager = rememberPermissionManager(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Obtenemos la ubicación del usuario para poder pasarla a la siguiente pantalla
    LaunchedEffect(locationPermissionManager.hasPermission) {
        if (locationPermissionManager.hasPermission) {
            try {
                val location = fusedLocationProviderClient.lastLocation.await()
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                // No hacer nada si falla, la ordenación por distancia simplemente no funcionará
            }
        } else {
            // Si no hay permiso, pedimos la ubicación.
            // La ordenación por distancia no estará disponible hasta que se conceda.
            locationPermissionManager.requestPermission()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Explorar por Categoría",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(categories) { category ->
                CategoryRow(
                    category = category,
                    onClick = {
                        onCategoryClick(category.destinationRoute, userLocation)
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun CategoryRow(category: ListCategory, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(category.title, fontWeight = FontWeight.SemiBold)
        },
        leadingContent = {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles")
        }
    )
}
