package com.example.parkmate.ui

import android.Manifest
import android.annotation.SuppressLint // Importa la anotación
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.Zone
import com.example.parkmate.screens.rememberPermissionManager
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.SearchBar
import com.example.parkmate.ui.components.ZoneDetailCard
import com.example.parkmate.utils.calculateCentroid
import com.example.parkmate.viewmodel.ZoneViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("MissingPermission") // <-- LA ANOTACIÓN SE MUEVE AQUÍ, A NIVEL DE FUNCIÓN
@Composable
fun MapScreen(
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val allZonesFromFirebase by zoneViewModel.zones.collectAsState()
    val isLoading by zoneViewModel.isLoading.collectAsState()

    // --- ESTADO PARA LA CÁMARA ---
    val cameraPositionState = rememberCameraPositionState {
        // Posición inicial por defecto (Lleida)
        position = CameraPosition.fromLatLngZoom(LatLng(41.6168, 0.6226), 12f)
    }

    // --- LÓGICA DE UBICACIÓN DEL USUARIO ---
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationHasBeenCentered by remember { mutableStateOf(false) } // Flag para centrar solo una vez

    // --- ESTADO PARA LA BÚSQUEDA Y SUGERENCIAS ---
    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<Zone>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }

    // --- LÓGICA DE BÚSQUEDA ---
    LaunchedEffect(searchQuery, allZonesFromFirebase) {
        if (searchQuery.isNotBlank()) {
            searchSuggestions = allZonesFromFirebase.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
            showSuggestions = true
        } else {
            searchSuggestions = emptyList()
            showSuggestions = false
        }
    }

    // --- ESTADO RESTANTE (BOTTOMSHEET, FILTROS, ETC.) ---
    val locationPermissionManager = rememberPermissionManager(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var showParking by remember { mutableStateOf(false) }
    var showGasStations by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(sheetState.isCollapsed) {
        if (sheetState.isCollapsed) selectedZone = null
    }

    // --- LÓGICA DE INICIALIZACIÓN ---
    LaunchedEffect(Unit) {
        locationPermissionManager.requestPermission()
    }

    // --- EFECTO PARA CENTRAR EL MAPA EN LA UBICACIÓN DEL USUARIO ---
    // Ya no necesita la anotación aquí
    LaunchedEffect(locationPermissionManager.hasPermission) {
        if (locationPermissionManager.hasPermission && !locationHasBeenCentered) {
            try {
                // Obtenemos la última ubicación conocida
                val location = fusedLocationProviderClient.lastLocation.await()
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    // Animamos la cámara a la ubicación del usuario con un zoom más cercano
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 15f), // Zoom más ampliado
                        1000 // Duración de la animación en ms
                    )
                    locationHasBeenCentered = true // Marcamos que ya hemos centrado el mapa
                }
            } catch (e: Exception) {
                // Manejar excepción si no se puede obtener la ubicación
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = { selectedZone?.let { ZoneDetailCard(zone = it) } },
        sheetPeekHeight = if (selectedZone != null) 150.dp else 0.dp,
        sheetGesturesEnabled = selectedZone != null
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapView(
                cameraPositionState = cameraPositionState,
                zones = if (showParking) allZonesFromFirebase else emptyList(),
                hasLocationPermission = locationPermissionManager.hasPermission,
                onZoneClick = { zone ->
                    selectedZone = zone
                    scope.launch { sheetState.expand() }
                },
                onMapClick = {
                    scope.launch { sheetState.collapse() }
                    showSuggestions = false
                }
            )

            // --- UI SUPERPUESTA CON BÚSQUEDA Y SUGERENCIAS ---
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
                        scope.launch {
                            val centroid = calculateCentroid(zone.vector)
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(centroid, 17f)
                            )
                        }
                        searchQuery = zone.name
                        showSuggestions = false
                    }
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
