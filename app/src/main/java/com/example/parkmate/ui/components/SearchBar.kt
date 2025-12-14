package com.example.parkmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.models.Zone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showParking: Boolean,
    onParkingToggle: () -> Unit,
    showGasStations: Boolean,
    onGasStationsToggle: () -> Unit,
    showSuggestions: Boolean,
    suggestions: List<Zone>,
    onSuggestionClick: (Zone) -> Unit
) {
    Column {
        // --- Barra de búsqueda ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar zona...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                shape = RoundedCornerShape(32.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // --- Botones de filtro con fondo ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                // AQUÍ ESTÁ EL CAMBIO: Se añade un `colors` personalizado al FilterChip
                val chipColors = FilterChipDefaults.filterChipColors(
                    // Color del contenedor con 70% de opacidad para que se vea el mapa detrás
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                )

                FilterChip(
                    selected = showParking,
                    onClick = onParkingToggle,
                    label = { Text("Parking") },
                    colors = chipColors
                )
                FilterChip(
                    selected = showGasStations,
                    onClick = onGasStationsToggle,
                    label = { Text("Gasolinera") },
                    colors = chipColors
                )
            }
        }

        // --- Lista de Sugerencias ---
        AnimatedVisibility(visible = showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    suggestions.take(5).forEachIndexed { index, zone ->
                        DropdownMenuItem(
                            text = { Text(zone.name) },
                            onClick = { onSuggestionClick(zone) }
                        )
                        if (index < suggestions.take(5).lastIndex) {
                            Divider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
