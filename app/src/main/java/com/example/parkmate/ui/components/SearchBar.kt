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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.models.Zone

// --- DATA CLASSES PARA AGRUPAR PARÁMETROS Y SOLUCIONAR EL ERROR ---

/**
 * Agrupa todos los estados (valores) que el SearchBar necesita para dibujarse.
 * Es inmutable para optimizar las recomposiciones.
 */
@Immutable
data class SearchBarState(
    val searchQuery: String,
    val showParking: Boolean,
    val showGasStations: Boolean,
    val showSuggestions: Boolean,
    val suggestions: List<Zone>
)

/**
 * Agrupa todas las acciones (eventos o lambdas) que el SearchBar puede ejecutar.
 */
@Immutable
data class SearchBarActions(
    val onSearchQueryChange: (String) -> Unit,
    val onParkingToggle: () -> Unit,
    val onGasStationsToggle: () -> Unit,
    val onSuggestionClick: (Zone) -> Unit
)

/**
 * El Composable principal, ahora con solo 2 parámetros,
 * solucionando el problema de SonarQube de forma definitiva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    state: SearchBarState,
    actions: SearchBarActions
) {
    Column {
        // --- Barra de búsqueda y filtros ---
        TextField(
            value = state.searchQuery,
            onValueChange = actions.onSearchQueryChange,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            )

            FilterChip(
                selected = state.showParking,
                onClick = actions.onParkingToggle,
                label = { Text("Parking") },
                colors = chipColors
            )
            FilterChip(
                selected = state.showGasStations,
                onClick = actions.onGasStationsToggle,
                label = { Text("Gasolinera") },
                colors = chipColors
            )
        }

        // --- Lista de Sugerencias ---
        AnimatedVisibility(visible = state.showSuggestions && state.suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    state.suggestions.take(5).forEachIndexed { index, zone ->
                        DropdownMenuItem(
                            text = { Text(zone.name) },
                            onClick = { actions.onSuggestionClick(zone) }
                        )
                        if (index < state.suggestions.take(5).lastIndex) {
                            Divider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
