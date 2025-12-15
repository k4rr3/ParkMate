package com.example.parkmate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkmate.data.models.InterestPoint
import com.example.parkmate.data.models.Zone
import com.example.parkmate.viewmodel.InterestPointViewModel
import com.example.parkmate.viewmodel.ZoneViewModel
import com.google.android.gms.maps.model.LatLng

private enum class ViewMode { MAP, LIST }

/**
 * Función principal que gestiona la vista de Mapa o Lista.
 */
@Composable
fun MapAndListScreen(
    zoneViewModel: ZoneViewModel = hiltViewModel(),
    interestPointViewModel: InterestPointViewModel = hiltViewModel()
) {
    var currentView by remember { mutableStateOf(ViewMode.MAP) }
    val listNavController = rememberNavController()
    var selectedListItem by remember { mutableStateOf<Any?>(null) }
    val allZones by zoneViewModel.zones.collectAsState()
    val allInterestPoints by interestPointViewModel.interestPoints.collectAsState()

    // Usamos un solo Box que contiene el Scaffold y el FAB
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentView) {
                    ViewMode.MAP -> {
                        MapScreen(
                            zoneViewModel = zoneViewModel,
                            interestPointViewModel = interestPointViewModel
                            // Los parámetros que daban error se han eliminado.
                        )
                    }
                    ViewMode.LIST -> {
                        ListView(
                            navController = listNavController,
                            zones = allZones,
                            interestPoints = allInterestPoints,
                            selectedItem = selectedListItem,
                            onSelectItem = { item -> selectedListItem = item },
                            onClearSelection = { selectedListItem = null }
                        )
                    }
                }
            }
        }

        // El FAB se coloca en el Box exterior y se alinea abajo a la izquierda (start)
        FloatingActionButton(
            onClick = {
                // La lógica del clic se extrae a una función para bajar la complejidad
                handleViewChange(
                    currentView = currentView,
                    listNavController = listNavController,
                    onViewChange = { newView -> currentView = newView },
                    onClearSelection = { selectedListItem = null }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (currentView == ViewMode.MAP) Icons.Default.List else Icons.Default.Map,
                contentDescription = if (currentView == ViewMode.MAP) "Ver lista" else "Ver mapa"
            )
        }
    }
}

/**
 * Función privada que aísla la lógica del onClick del FAB para reducir la complejidad
 * de la función principal.
 */
private fun handleViewChange(
    currentView: ViewMode,
    listNavController: NavHostController,
    onViewChange: (ViewMode) -> Unit,
    onClearSelection: () -> Unit
) {
    if (currentView == ViewMode.LIST) {
        onClearSelection()
        // Volvemos a la pantalla principal de la lista si estábamos en un detalle
        if (listNavController.currentDestination?.route?.startsWith("category_detail") == true) {
            listNavController.popBackStack(route = "category_list", inclusive = false)
        }
    }
    // Cambiamos la vista
    onViewChange(if (currentView == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP)
}


/**
 * Un Composable que encapsula la navegación de la vista de lista.
 */
@Composable
private fun ListView(
    navController: NavHostController,
    zones: List<Zone>,
    interestPoints: List<InterestPoint>,
    selectedItem: Any?,
    onSelectItem: (Any) -> Unit,
    onClearSelection: () -> Unit
) {
    NavHost(navController = navController, startDestination = "category_list") {
        composable("category_list") {
            ListScreen(
                onCategoryClick = { route, userLocation ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("user_location", userLocation)
                    navController.navigate(route)
                }
            )
        }
        composable("category_detail/{type}") { backStackEntry ->
            val categoryType = backStackEntry.arguments?.getString("type") ?: ""
            val userLocation = navController.previousBackStackEntry?.savedStateHandle?.get<LatLng?>("user_location")

            // Construimos el objeto State para pasarlo al Composable
            val detailState = CategoryDetailState(
                categoryType = categoryType,
                userLocation = userLocation,
                zones = zones,
                interestPoints = interestPoints
            )

            CategoryDetailScreen(
                state = detailState,
                selectedItem = selectedItem,
                onItemClick = onSelectItem,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClearSelection = onClearSelection
            )
        }
    }
}
