package com.example.parkmate.ui

import android.Manifest // <-- Asegúrate de que este import está presente
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.parkmate.mock.ParkingSpot
import com.example.parkmate.mock.parkingSpots
// CAMBIO 1: Importa el gestor de permisos desde el paquete 'screens' donde ahora reside
import com.example.parkmate.screens.rememberPermissionManager
import com.example.parkmate.ui.components.FilterBar
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.ParkingSpotDetailCard
import com.example.parkmate.ui.components.SearchBar
import com.example.parkmate.utils.rememberDeviceLocation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapScreen() {
    // CAMBIO 2: Llama a rememberPermissionManager pasándole el permiso de ubicación específico.
    val locationPermissionManager = rememberPermissionManager(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Parking") }
    val spots = if (searchQuery.isEmpty()) {
        parkingSpots
    } else {
        parkingSpots.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // --- Lógica del BottomSheet (sin cambios) ---
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    if (sheetState.isCollapsed) {
        LaunchedEffect(key1 = sheetState.isCollapsed) {
            selectedSpot = null
        }
    }

    // Pide el permiso al iniciar la pantalla si no lo tiene.
    LaunchedEffect(Unit) {
        if (!locationPermissionManager.hasPermission) {
            // CAMBIO 3: Llama a `requestPermission()`, el nuevo método para un solo permiso.
            locationPermissionManager.requestPermission()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            selectedSpot?.let { spot ->
                ParkingSpotDetailCard(spot = spot)
            }
        },
        sheetPeekHeight = if (selectedSpot != null) 120.dp else 0.dp
    ) {
        // MapView siempre se muestra, pero su comportamiento interno cambia según el permiso.
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                FilterBar(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
                MapView(
                    parkingSpots = spots,
                    // CAMBIO 4: Pasamos el estado de nuestro gestor de permisos específico.
                    hasLocationPermission = locationPermissionManager.hasPermission,
                    onSpotClick = { spot ->
                        selectedSpot = spot
                        scope.launch { sheetState.expand() }
                    },
                    onMapClick = {
                        scope.launch { sheetState.collapse() }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
