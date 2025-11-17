package com.example.parkmate.ui

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.Zone
import com.example.parkmate.screens.rememberPermissionManager
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.SearchBar
import com.example.parkmate.ui.components.ZoneDetailCard
import com.example.parkmate.viewmodel.ZoneViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapScreen(
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val allZonesFromFirebase by zoneViewModel.zones.collectAsState()
    val isLoading by zoneViewModel.isLoading.collectAsState()

    // --- LOG PARA VER SI LA UI RECIBE LOS DATOS ---
    Log.d("DEBUG_MAPSCREEN", "MapScreen redibujado con: ${allZonesFromFirebase.size} zonas.")
    // ------------------------------------------

    val locationPermissionManager = rememberPermissionManager(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var searchQuery by remember { mutableStateOf("") }
    var showParking by remember { mutableStateOf(true) }
    var showGasStations by remember { mutableStateOf(false) }

    val filteredZones = remember(allZonesFromFirebase, searchQuery) {
        if (searchQuery.isBlank()) {
            allZonesFromFirebase
        } else {
            allZonesFromFirebase.filter { zone ->
                zone.name.contains(searchQuery, ignoreCase = true) ||
                        zone.tariff.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(sheetState.isCollapsed) {
        if (sheetState.isCollapsed) {
            selectedZone = null
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionManager.requestPermission()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            selectedZone?.let { zone ->
                ZoneDetailCard(zone = zone)
            }
        },
        sheetPeekHeight = if (selectedZone != null) 150.dp else 0.dp,
        sheetGesturesEnabled = selectedZone != null
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapView(
                zones = if (showParking) filteredZones else emptyList(),
                hasLocationPermission = locationPermissionManager.hasPermission,
                onZoneClick = { zone ->
                    selectedZone = zone
                    scope.launch { sheetState.expand() }
                },
                onMapClick = {
                    scope.launch { sheetState.collapse() }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { newQuery -> searchQuery = newQuery },
                    showParking = showParking,
                    onParkingToggle = { showParking = !showParking },
                    showGasStations = showGasStations,
                    onGasStationsToggle = { showGasStations = !showGasStations }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
