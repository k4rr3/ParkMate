package com.example.parkmate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.data.models.Zone
import com.example.parkmate.utils.calculateCentroid
import com.example.parkmate.viewmodel.ProfileViewModel
import com.example.parkmate.viewmodel.VehicleViewModel
import com.google.firebase.firestore.GeoPoint
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
    profileViewModel: ProfileViewModel = hiltViewModel(),
    vehicleViewModel: VehicleViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        vehicleViewModel.loadUserVehicles()
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var showVehicleSelector by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }

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
    val hasVehicles = vehicleViewModel.vehicles.collectAsState().value.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Añadimos estos estados
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Éxito del pago y actualización del vehículo
    val onPaymentSuccess = { vehicle: Vehicle ->
        // En lugar de snackbar, mostramos diálogo centrado
        showSuccessDialog = true
        successMessage = "Payment made!\n${vehicle.name} parked correctly\nuntil ${
            selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        }"
    }



// Y al final del composable, después de todos los otros diálogos:

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                // Opcional: podrías querer volver atrás o cerrar bottom sheet aquí
                // Por ejemplo: onNavigateBack?.invoke()
            },
            title = {
                Text(
                    text = "Success!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = successMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text("Accept")
                }
            },
            // Opcional: puedes quitar el dismissButton si quieres forzar que pulse Aceptar
        )
    }

    // Diálogo de selección de vehículo
    if (showVehicleSelector) {
        VehicleSelectionDialog(
            vehicles = vehicleViewModel.vehicles.collectAsState().value,
            onVehicleSelected = { vehicle ->
                selectedVehicle = vehicle
                showVehicleSelector = false

                // Realizamos el pago
                profileViewModel.spendCredits(calculatedCost) { success ->
                    if (success && selectedVehicle != null) {
                        // Preparamos la actualización del vehículo
                        val centroid = calculateCentroid(zone.vector) // Asegúrate que esta función existe
                        val updates = mapOf(
                            "parkingLocation" to GeoPoint(centroid.latitude, centroid.longitude),
                            "parkingEndTime" to LocalDateTime.now()
                                .with(selectedEndTime)
                                .toString(), // formato ISO o el que prefieras
                            "activeZoneId" to zone.id
                        )

                        vehicleViewModel.updateVehicle(vehicle.id, updates as Map<String, String>)
                        onPaymentSuccess(vehicle)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Error processing payment or updating vehicle",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            },
            onDismiss = { showVehicleSelector = false }
        )
    }


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
                    Text(text = "Fee: ${zone.tariff}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Capacity: ${zone.capacity}", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Schedule: ${zone.schedule}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your credits: $userCredits",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estimated cost: $calculatedCost credits (until ${
                        selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    })",
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
                            contentDescription = "Navigate",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Navigate")
                    }

                    Button(
                        onClick = { showTimePicker = true },
                        enabled = hasEnoughCredits && hasVehicles,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasEnoughCredits && hasVehicles)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payment,
                            contentDescription = "Pay zone",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Pay zone")
                    }
                }

                // Mensajes de estado
                when {
                    !hasVehicles -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Add a vehicle to be able to pay for this area",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    !hasEnoughCredits -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Insufficient credits for the selected time",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }


    // Diálogo de selección de hora
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Confirm zone payment") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Until: ${
                            selectedEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        }",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total cost: $calculatedCost credits",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasEnoughCredits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        if (hasVehicles) {
                            showVehicleSelector = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("You have no registered vehicles")
                            }
                        }
                    },
                    enabled = hasEnoughCredits && hasVehicles
                ) {
                    Text("Continue → Select vehicle")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleSelectionDialog(
    vehicles: List<Vehicle>,
    onVehicleSelected: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select the vehicle to park") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(vehicles) { vehicle ->
                    ListItem(
                        headlineContent = { Text(vehicle.name) },
                        supportingContent = { Text("${vehicle.plate} • ${vehicle.brand} ${vehicle.model}") },
                        leadingContent = {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVehicleSelected(vehicle) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}