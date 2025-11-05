// In: app/src/main/java/com/example/parkmate/ui/components/ParkingSpotDetailCard.kt
package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.mock.ParkingSpot

@Composable
fun ParkingSpotDetailCard(spot: ParkingSpot) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = spot.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Available Places: ${spot.spots}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Prize: ${spot.price}",
                style = MaterialTheme.typography.bodyLarge
            )
            // Aquí puedes añadir más detalles, como botones, imágenes, etc.
        }
    }
}
