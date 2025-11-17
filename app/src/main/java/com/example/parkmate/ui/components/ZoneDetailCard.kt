package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.data.models.Zone

@Composable
fun ZoneDetailCard(zone: Zone) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Nombre de la zona
        Text(
            text = zone.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Filas de información
        InfoRow(label = "Capacidad:", value = zone.capacity)
        InfoRow(label = "Horario:", value = zone.schedule)
        InfoRow(label = "Tarifa:", value = zone.tariff)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.width(90.dp) // Ancho fijo para alinear los valores
        )
        Text(
            text = value,
            fontSize = 14.sp,
            lineHeight = 20.sp // Mejora la legibilidad para textos largos
        )
    }
}
