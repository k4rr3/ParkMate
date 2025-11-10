package com.example.parkmate.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.viewmodel.VehicleViewModel
import com.example.parkmate.data.models.Vehicle

@Composable
fun CarDetailsScreenWrapper(
    vehicleId: String, // use ID instead of full Vehicle
    viewModel: VehicleViewModel = hiltViewModel()
) {
    // VehicleState is a Flow or StateFlow in the ViewModel
    val vehicleState = viewModel.getVehicleById(vehicleId).collectAsState(initial = null)
    val vehicle = vehicleState.value

    if (vehicle != null) {
        CarDetailsScreen(vehicle = vehicle)
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
