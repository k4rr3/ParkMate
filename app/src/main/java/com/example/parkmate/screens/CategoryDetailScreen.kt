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
import androidx.compose.runtime.Immutable
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

// DATA CLASS PARA AGRUPAR PARÁMETROS Y REDUCIR SU NÚMERO
@Immutable
data class CategoryDetailState(
    val categoryType: String,
    val userLocation: LatLng?,
    val zones: List<Zone>,
    val interestPoints: List<InterestPoint>
)

/**
 * Función principal que ahora actúa como un simple contenedor.
 * Su complejidad es muy baja.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    state: CategoryDetailState,
    selectedItem: Any?,
    onItemClick: (item: Any) -> Unit,
    onNavigateBack: () -> Unit,
    onClearSelection: () -> Unit
) {
    val title = if (selectedItem != null) {
        "Detalles"
    } else {
        when (state.categoryType) {
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedItem != null) {
            ItemDetailView(
                modifier = Modifier.padding(innerPadding),
                selectedItem = selectedItem
            )
        } else {
            ItemListView(
                modifier = Modifier.padding(innerPadding),
                state = state,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Composable que se encarga EXCLUSIVAMENTE de mostrar el detalle de un ítem.
 */
@Composable
private fun ItemDetailView(
    modifier: Modifier = Modifier,
    selectedItem: Any
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        when (selectedItem) {
            is Zone -> ZoneDetailCard(
                zone = selectedItem,
                onNavigateClick = {
                    val destination = calculateCentroid(selectedItem.vector)
                    val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
                // El parámetro onShowOnMapClick se elimina porque no existe en tu tarjeta
            )
            is InterestPoint -> InterestPointDetailCard(
                point = selectedItem,
                onNavigateClick = {
                    val destination = selectedItem.location
                    val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
                // El parámetro onShowOnMapClick se elimina porque no existe en tu tarjeta
            )
        }
    }
}

/**
 * Composable que se encarga EXCLUSIVAMENTE de mostrar la lista de ítems.
 */
@Composable
private fun ItemListView(
    modifier: Modifier = Modifier,
    state: CategoryDetailState,
    onItemClick: (item: Any) -> Unit
) {
    // La lógica de ordenación ahora vive aquí, en un contexto más pequeño.
    val sortedItems = remember(state) {
        when (state.categoryType) {
            "zones" -> state.zones.map { zone ->
                val centroid = calculateCentroid(zone.vector)
                val distance = state.userLocation?.let {
                    calculateDistance(it.latitude, it.longitude, centroid.latitude, centroid.longitude)
                }
                ListItemData(data = zone, name = zone.name, distance = distance)
            }.let { if (state.userLocation != null) it.sortedBy { item -> item.distance } else it }

            "gas_stations" -> state.interestPoints
                .filter { it.type == "GasStation" }
                .map { point ->
                    val distance = state.userLocation?.let {
                        calculateDistance(it.latitude, it.longitude, point.location.latitude, point.location.longitude)
                    }
                    ListItemData(data = point, name = point.name, distance = distance)
                }.let { if (state.userLocation != null) it.sortedBy { item -> item.distance } else it }

            else -> emptyList()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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

// Data class para la lista. No cambia.
data class ListItemData(val data: Any, val name: String, val distance: Double?)

// Composable para cada tarjeta de la lista. No cambia.
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
