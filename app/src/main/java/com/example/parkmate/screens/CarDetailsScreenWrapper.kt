package com.example.parkmate.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.viewmodel.VehicleViewModel
import com.example.parkmate.data.models.Vehicle

@Composable
fun CarDetailsScreenWrapper(
    vehicleId: String,
    navController: NavHostController,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicle by viewModel.getVehicleByIdRealtime(vehicleId).collectAsState()

    if (vehicle != null) {
        CarDetailsScreen(vehicleId = vehicleId,navController = navController)
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

