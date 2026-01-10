package com.example.parkmate.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.InterestPoint
import com.example.parkmate.data.models.Zone
import com.example.parkmate.screens.PermissionManager
import com.example.parkmate.screens.rememberPermissionManager
import com.example.parkmate.ui.components.*
import com.example.parkmate.utils.calculateCentroid
import com.example.parkmate.viewmodel.InterestPointViewModel
import com.example.parkmate.viewmodel.ProfileViewModel
import com.example.parkmate.viewmodel.ZoneViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    zoneViewModel: ZoneViewModel = hiltViewModel(),
    interestPointViewModel: InterestPointViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val allZonesFromFirebase by zoneViewModel.zones.collectAsState()
    val allInterestPoints by interestPointViewModel.interestPoints.collectAsState()
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
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    var selectedInterestPoint by remember { mutableStateOf<InterestPoint?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    HandleSearchQuery(searchQuery, allZonesFromFirebase) { suggestions, show ->
        searchSuggestions = suggestions
        showSuggestions = show
    }

    HandleSheetCollapse(sheetState) {
        selectedZone = null
        selectedInterestPoint = null
    }

    RequestPermissionEffect(locationPermissionManager)
    CenterLocationOnce(locationPermissionManager, locationHasBeenCentered, fusedLocationProviderClient, cameraPositionState) {
        locationHasBeenCentered = true
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetContent(selectedZone, selectedInterestPoint, context, profileViewModel)
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
                gasStations = if (showGasStations) allInterestPoints.filter { it.type == "GasStation" } else emptyList(),
                hasLocationPermission = locationPermissionManager.hasPermission,
                onZoneClick = { zone ->
                    keyboardController?.hide()
                    selectedInterestPoint = null
                    selectedZone = zone
                    scope.launch { sheetState.expand() }
                },
                onGasStationClick = { station ->
                    keyboardController?.hide()
                    selectedZone = null
                    selectedInterestPoint = station
                    scope.launch { sheetState.expand() }
                },
                onMapClick = {
                    keyboardController?.hide()
                    scope.launch { sheetState.collapse() }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // ===================== INICIO DEL CAMBIO =====================

                // 1. Se construye el objeto de estado con los valores que ya existen
                val searchState = SearchBarState(
                    searchQuery = searchQuery,
                    showParking = showParking,
                    showGasStations = showGasStations,
                    showSuggestions = showSuggestions,
                    suggestions = searchSuggestions
                )

                // 2. Se construye el objeto de acciones con las lambdas que ya existen
                val searchActions = SearchBarActions(
                    onSearchQueryChange = { searchQuery = it },
                    onParkingToggle = { showParking = !showParking },
                    onGasStationsToggle = { showGasStations = !showGasStations },
                    onSuggestionClick = { zone ->
                        keyboardController?.hide()
                        searchQuery = zone.name
                        showSuggestions = false
                        selectedInterestPoint = null
                        selectedZone = zone
                        scope.launch {
                            val centroid = calculateCentroid(zone.vector)
                            sheetState.expand()
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(centroid, 17f))
                        }
                    }
                )

                // 3. Se llama al nuevo SearchBar con solo dos parámetros
                SearchBar(
                    state = searchState,
                    actions = searchActions
                )

                // ====================== FIN DEL CAMBIO ======================
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HandleSearchQuery(query: String, zones: List<Zone>, onUpdate: (List<Zone>, Boolean) -> Unit) {
    LaunchedEffect(query, zones) {
        if (query.isNotBlank()) {
            onUpdate(zones.filter { it.name.contains(query, ignoreCase = true) }, true)
        } else {
            onUpdate(emptyList(), false)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HandleSheetCollapse(sheetState: BottomSheetState, onCollapse: () -> Unit) {
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.isCollapsed }.collect { isCollapsed ->
            if (isCollapsed) onCollapse()
        }
    }
}

@Composable
fun RequestPermissionEffect(permissionManager: PermissionManager) {
    LaunchedEffect(Unit) {
        permissionManager.requestPermission()
    }
}

@Composable
fun CenterLocationOnce(
    permissionManager: PermissionManager,
    locationCentered: Boolean,
    client: FusedLocationProviderClient,
    camera: CameraPositionState,
    onCentered: () -> Unit
) {
    LaunchedEffect(permissionManager.hasPermission) {
        if (permissionManager.hasPermission && !locationCentered) {
            try {
                val location = client.lastLocation.await()
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    camera.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f), 1000)
                    onCentered()
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error al obtener la ubicación", e)
            }
        }
    }
}

@Composable
fun BottomSheetContent(selectedZone: Zone?, selectedPoint: InterestPoint?, context: android.content.Context, profileViewModel: ProfileViewModel) {
    val credits by profileViewModel.credits.collectAsState()
    when {
        selectedZone != null -> {
            ZoneDetailCard(
                zone = selectedZone,
                userCredits = credits,
                onNavigateClick = {
                    val destination = calculateCentroid(selectedZone.vector)
                    val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            )
        }
        selectedPoint != null -> {
            InterestPointDetailCard(
                point = selectedPoint,
                onNavigateClick = {
                    val destination = selectedPoint.location
                    val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            )
        }
        else -> Spacer(modifier = Modifier.height(1.dp))
    }
}
