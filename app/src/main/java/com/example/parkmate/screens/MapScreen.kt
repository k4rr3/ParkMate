package com.example.parkmate.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.InterestPoint // <-- NUEVA IMPORTACIÓN
import com.example.parkmate.data.models.Zone
import com.example.parkmate.screens.rememberPermissionManager
import com.example.parkmate.ui.components.InterestPointDetailCard // <-- NUEVA IMPORTACIÓN
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.SearchBar
import com.example.parkmate.ui.components.ZoneDetailCard
import com.example.parkmate.utils.calculateCentroid // Cambiado desde utils
import com.example.parkmate.viewmodel.InterestPointViewModel // <-- NUEVA IMPORTACIÓN
import com.example.parkmate.viewmodel.ZoneViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    // 1. Inyectamos ambos ViewModels
    zoneViewModel: ZoneViewModel = hiltViewModel(),
    interestPointViewModel: InterestPointViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 2. Obtenemos los datos de cada ViewModel
    val allZonesFromFirebase by zoneViewModel.zones.collectAsState()
    val allInterestPoints by interestPointViewModel.interestPoints.collectAsState()

    // 3. Combinamos los estados de carga
    val zonesLoading by zoneViewModel.isLoading.collectAsState()
    val pointsLoading by interestPointViewModel.isLoading.collectAsState()
    val isLoading = zonesLoading || pointsLoading

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.6168, 0.6226), 12f)
    }

    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationHasBeenCentered by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<Zone>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    val locationPermissionManager = rememberPermissionManager(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var showParking by remember { mutableStateOf(false) }
    var showGasStations by remember { mutableStateOf(false) }

    // 4. NUEVO ESTADO para el punto de interés seleccionado
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    var selectedInterestPoint by remember { mutableStateOf<InterestPoint?>(null) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    // Función para limpiar selecciones y cerrar el menú
    val clearSelectionsAndCollapse = {
        keyboardController?.hide()
        selectedZone = null
        selectedInterestPoint = null
        scope.launch { sheetState.collapse() }
    }

    LaunchedEffect(searchQuery, allZonesFromFirebase) {
        if (searchQuery.isNotBlank()) {
            searchSuggestions = allZonesFromFirebase.filter { it.name.contains(searchQuery, ignoreCase = true) }
            showSuggestions = true
        } else {
            searchSuggestions = emptyList()
            showSuggestions = false
        }
    }

    LaunchedEffect(sheetState.isCollapsed) {
        if (sheetState.isCollapsed) {
            selectedZone = null
            selectedInterestPoint = null
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionManager.requestPermission()
    }

    LaunchedEffect(locationPermissionManager.hasPermission) {
        if (locationPermissionManager.hasPermission && !locationHasBeenCentered) {
            try {
                val location = fusedLocationProviderClient.lastLocation.await()
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f), 1000)
                    locationHasBeenCentered = true
                }
            } catch (e: Exception) { /* Ignorar error */ }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        // 5. Contenido del BottomSheet ahora es DINÁMICO
        sheetContent = {
            if (selectedZone != null) {
                ZoneDetailCard(
                    zone = selectedZone!!,
                    onNavigateClick = {
                        val destination = calculateCentroid(selectedZone!!.vector)
                        val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                )
            } else if (selectedInterestPoint != null) {
                InterestPointDetailCard(
                    point = selectedInterestPoint!!,
                    onNavigateClick = {
                        val destination = selectedInterestPoint!!.location
                        val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp)) // Contenido vacío para evitar errores
            }
        },
        sheetPeekHeight = if (selectedZone != null || selectedInterestPoint != null) 200.dp else 0.dp,
        sheetGesturesEnabled = selectedZone != null || selectedInterestPoint != null
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapView(
                cameraPositionState = cameraPositionState,
                zones = if (showParking) allZonesFromFirebase else emptyList(),
                // 6. Pasamos la lista de gasolineras filtrada
                gasStations = if (showGasStations) {
                    allInterestPoints.filter { it.type == "GasStation" }
                } else {
                    emptyList()
                },
                hasLocationPermission = locationPermissionManager.hasPermission,
                // 7. Implementamos los callbacks de clic
                onZoneClick = { zone ->
                    keyboardController?.hide()
                    selectedInterestPoint = null // Limpia la otra selección
                    selectedZone = zone
                    scope.launch { sheetState.expand() }
                },
                onGasStationClick = { station ->
                    keyboardController?.hide()
                    selectedZone = null // Limpia la otra selección
                    selectedInterestPoint = station
                    scope.launch { sheetState.expand() }
                },
                onMapClick = {
                    clearSelectionsAndCollapse() // Usa la función de limpieza
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
                    onGasStationsToggle = { showGasStations = !showGasStations },
                    showSuggestions = showSuggestions,
                    suggestions = searchSuggestions,
                    onSuggestionClick = { zone ->
                        keyboardController?.hide()
                        searchQuery = zone.name
                        showSuggestions = false
                        selectedInterestPoint = null // Limpia la otra selección
                        selectedZone = zone
                        scope.launch {
                            val centroid = calculateCentroid(zone.vector)
                            sheetState.expand()
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(centroid, 17f))
                        }
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
