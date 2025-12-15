package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.Zone
import com.example.parkmate.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailCard(
    zone: Zone,
    onNavigateClick: () -> Unit,
    userCredits: Int = 0,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().plusHours(1).hour,
        initialMinute = 0
    )

    val selectedEndTime = LocalTime.of(timePickerState.hour, timePickerState.minute)

    val hourlyRate = zone.tariff
        .replace(Regex("[^0-9.]"), "")
        .toDoubleOrNull() ?: 1.0

    val calculatedCost = remember(selectedEndTime) {
        val now = LocalDateTime.now()
        val endDateTime = LocalDateTime.now().with(selectedEndTime)

        val durationHours = if (endDateTime.isBefore(now)) {
            val tomorrow = now.plusDays(1).with(selectedEndTime)
            java.time.Duration.between(now, tomorrow).toHours().toDouble()
        } else {
            java.time.Duration.between(now, endDateTime).toHours().toDouble()
        }

        val hoursToPay = ceil(durationHours).toInt().coerceAtLeast(1)
        (hoursToPay * hourlyRate).toInt()
    }

    val hasEnoughCredits = userCredits >= calculatedCost

    // Para mostrar el Snackbar
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Confirmación de pago exitoso
    val onPaymentSuccess: () -> Unit = {
        scope.launch {
            scaffoldState.showSnackbar(
                message = "¡Pago realizado! Zona activa hasta las ${selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
        }
    }

    // Error de pago
    val onPaymentError: () -> Unit = {
        scope.launch {
            scaffoldState.showSnackbar(
                message = "Error al realizar el pago. Créditos insuficientes o fallo de conexión.",
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) }
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(padding),
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

                Text(
                    text = "Tus créditos: $userCredits",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Coste estimado: $calculatedCost créditos (hasta las ${selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasEnoughCredits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "Navegar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Navegar")
                    }

                    Button(
                        onClick = { showTimePicker = true },
                        enabled = userCredits >= calculatedCost,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userCredits >= calculatedCost)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payment,
                            contentDescription = "Pagar zona",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Pagar zona")
                    }
                }

                if (userCredits < calculatedCost) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Créditos insuficientes para el tiempo seleccionado",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Confirmar pago de zona") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Hasta las: ${selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Coste total: $calculatedCost créditos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasEnoughCredits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.spendCredits(calculatedCost) { success ->
                            if (success) {
                                onPaymentSuccess()
                            } else {
                                onPaymentError()
                            }
                        }
                        showTimePicker = false
                    },
                    enabled = hasEnoughCredits
                ) {
                    Text("Pagar $calculatedCost créditos")
                }
            }
        )
    }
}