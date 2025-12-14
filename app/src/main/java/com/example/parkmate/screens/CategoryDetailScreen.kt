package com.example.parkmate.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.models.InterestPoint
import com.example.parkmate.data.models.Zone
import com.example.parkmate.ui.components.InterestPointDetailCard
import com.example.parkmate.ui.components.ZoneDetailCard
import com.example.parkmate.utils.calculateCentroid
import com.example.parkmate.utils.calculateDistance
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryType: String,
    userLocation: LatLng?,
    zones: List<Zone>,
    interestPoints: List<InterestPoint>,
    selectedItem: Any?,
    onItemClick: (item: Any) -> Unit,
    onNavigateBack: () -> Unit,
    onClearSelection: () -> Unit
) {
    val context = LocalContext.current
    val title = if (selectedItem != null) {
        "Detalles"
    } else {
        when (categoryType) {
            "zones" -> "Zonas de Aparcamiento"
            "gas_stations" -> "Gasolineras"
            else -> "Detalles"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedItem != null) {
                            onClearSelection()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedItem != null) {
            // --- VISTA DE DETALLE (SECCIÓN CORREGIDA) ---
            // El Box ahora gestiona el padding exterior de la tarjeta.
            Box(
                modifier = Modifier
                    .padding(innerPadding) // Padding del Scaffold
                    .padding(16.dp)       // Padding adicional nuestro
                    .fillMaxSize()
            ) {
                when (selectedItem) {
                    is Zone -> ZoneDetailCard(
                        // No le pasamos un modifier, usará el suyo por defecto
                        zone = selectedItem,
                        onNavigateClick = {
                            val destination = calculateCentroid(selectedItem.vector)
                            val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        }
                    )
                    is InterestPoint -> InterestPointDetailCard(
                        // No le pasamos un modifier, usará el suyo por defecto
                        point = selectedItem,
                        onNavigateClick = {
                            val destination = selectedItem.location
                            val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        }
                    )
                }
            }
        } else {
            // --- VISTA DE LISTA (sin cambios) ---
            val sortedItems = remember(userLocation, zones, interestPoints) {
                when (categoryType) {
                    "zones" -> zones.map { zone ->
                        val centroid = calculateCentroid(zone.vector)
                        val distance = userLocation?.let {
                            calculateDistance(it.latitude, it.longitude, centroid.latitude, centroid.longitude)
                        }
                        ListItemData(data = zone, name = zone.name, distance = distance)
                    }.let { if (userLocation != null) it.sortedBy { item -> item.distance } else it }

                    "gas_stations" -> interestPoints
                        .filter { it.type == "GasStation" }
                        .map { point ->
                            val distance = userLocation?.let {
                                calculateDistance(it.latitude, it.longitude, point.location.latitude, point.location.longitude)
                            }
                            ListItemData(data = point, name = point.name, distance = distance)
                        }.let { if (userLocation != null) it.sortedBy { item -> item.distance } else it }

                    else -> emptyList()
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(sortedItems) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item.data) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class ListItemData(val data: Any, val name: String, val distance: Double?)

@Composable
fun ItemCard(item: ListItemData, onClick: () -> Unit) {
    val df = DecimalFormat("#.##")
    val distanceText = item.distance?.let { "${df.format(it)} km" } ?: "Distancia no disponible"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(distanceText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
