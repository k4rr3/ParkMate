package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.models.Zone

@Composable
fun ZoneDetailCard(
    zone: Zone,
    onNavigateClick: () -> Unit // <-- NUEVO: Lambda para manejar el clic de navegación
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = zone.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tarifa: ${zone.tariff}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Capacidad: ${zone.capacity}", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Horario: ${zone.schedule}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // --- AQUÍ ESTÁ EL NUEVO BOTÓN ---
            Button(
                onClick = onNavigateClick, // Llama a la función que nos pasan desde MapScreen
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navigate Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Navegar")
            }
        }
    }
}
