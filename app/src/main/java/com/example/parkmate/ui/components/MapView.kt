package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.parkmate.mock.ParkingSpot

@Composable
fun MapView(
    parkingSpots: List<ParkingSpot>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Text("MAP")
    }
}
