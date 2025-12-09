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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkmate.viewmodel.InterestPointViewModel
import com.example.parkmate.viewmodel.ZoneViewModel
import com.google.android.gms.maps.model.LatLng

private enum class ViewMode { MAP, LIST }

@Composable
fun MapAndListScreen(
    zoneViewModel: ZoneViewModel = hiltViewModel(),
    interestPointViewModel: InterestPointViewModel = hiltViewModel()
) {
    var currentView by remember { mutableStateOf(ViewMode.MAP) }
    val listNavController = rememberNavController()

    // Estado para saber qué ítem de la lista está seleccionado
    var selectedListItem by remember { mutableStateOf<Any?>(null) }

    val allZones by zoneViewModel.zones.collectAsState()
    val allInterestPoints by interestPointViewModel.interestPoints.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentView) {
                    ViewMode.MAP -> {
                        // MapScreen vuelve a ser simple.
                        MapScreen(
                            zoneViewModel = zoneViewModel,
                            interestPointViewModel = interestPointViewModel
                        )
                    }
                    ViewMode.LIST -> {
                        NavHost(navController = listNavController, startDestination = "category_list") {
                            composable("category_list") {
                                ListScreen(
                                    onCategoryClick = { route, userLocation ->
                                        listNavController.currentBackStackEntry?.savedStateHandle?.set("user_location", userLocation)
                                        listNavController.navigate(route)
                                    }
                                )
                            }
                            composable("category_detail/{type}") { backStackEntry ->
                                val categoryType = backStackEntry.arguments?.getString("type") ?: ""
                                val userLocation = listNavController.previousBackStackEntry?.savedStateHandle?.get<LatLng?>("user_location")

                                CategoryDetailScreen(
                                    categoryType = categoryType,
                                    userLocation = userLocation,
                                    zones = allZones,
                                    interestPoints = allInterestPoints,
                                    selectedItem = selectedListItem,
                                    onItemClick = { item ->
                                        selectedListItem = item // Guardamos el ítem para mostrar su detalle
                                    },
                                    onNavigateBack = {
                                        listNavController.popBackStack() // Volver a la pantalla de categorías
                                    },
                                    onClearSelection = {
                                        selectedListItem = null // Limpiar selección para volver a la lista
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botón flotante para cambiar entre mapa y lista
        FloatingActionButton(
            onClick = {
                // Si cambiamos de vista, reseteamos el estado de selección de la lista
                if (currentView == ViewMode.LIST) {
                    selectedListItem = null
                    // Volvemos a la pantalla principal de la lista si estábamos en un detalle
                    if (listNavController.currentDestination?.route?.startsWith("category_detail") == true) {
                        listNavController.popBackStack(route = "category_list", inclusive = false)
                    }
                }
                currentView = if (currentView == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP
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
