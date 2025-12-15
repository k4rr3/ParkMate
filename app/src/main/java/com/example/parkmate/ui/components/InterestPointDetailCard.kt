package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.data.models.InterestPoint

@Composable
fun InterestPointDetailCard(
    point: InterestPoint,
    onNavigateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- La información de la gasolinera no cambia ---
            Text(
                text = point.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Horario: ${point.schedule}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            point.price?.let { prices ->
                Text(
                    text = "Precios del combustible:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FuelPriceColumn(fuelType = "SP95", price = prices.G95)
                    FuelPriceColumn(fuelType = "SP98", price = prices.G98)
                    FuelPriceColumn(fuelType = "Diésel", price = prices.DA)
                    FuelPriceColumn(fuelType = "Diésel+", price = prices.DA_plus)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            // AFEGIR BOTÓ PAGO


            Button(
                onClick = onNavigateClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp) // Añadimos un poco de padding vertical
            ) {
                // 2. El icono y el texto se mantienen igual que en la versión "bonita".
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Navegar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Navegar")
            }
        }
    }
}

@Composable
private fun FuelPriceColumn(fuelType: String, price: String) {
    if (price.isNotBlank()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = fuelType, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "$price €",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
