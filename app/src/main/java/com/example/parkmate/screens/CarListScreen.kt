package com.example.parkmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.google.firebase.firestore.GeoPoint
import com.example.parkmate.mock.*
import com.example.parkmate.ui.theme.*
import com.example.parkmate.viewmodel.VehicleViewModel
import com.example.parkmate.data.models.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    navController: NavHostController,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()
    var showAddCarForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserVehicles()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCarForm = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Vehicle"
                )
            }
        }
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No vehicles yet. Add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleCard(navController, vehicle)
                }
            }
        }

        if (showAddCarForm) {
            AddCarForm(
                onDismiss = { showAddCarForm = false },
                onAddCar = { newVehicle ->
                    viewModel.addVehicle(newVehicle)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(navController: NavHostController, vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { navController.navigate(Screen.CarDetailsScreen.route + "/${vehicle.id}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CarListIcon),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = vehicle.plate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Optional info text
            Text(
                text = "Fuel: ${vehicle.fuelType}  |  Year: ${vehicle.year}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action button — maybe “Locate” or “View Details”
            Button(
                onClick = { navController.navigate(Screen.CarDetailsScreen.route + "/${vehicle.id}") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Details", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun StatusIndicator(status: VehicleStatus, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val color = when (status) {
            VehicleStatus.PARKED -> Green
            VehicleStatus.DRIVING -> MaterialTheme.colorScheme.primary
            VehicleStatus.EXPIRED -> MaterialTheme.colorScheme.error
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(4.dp))

        val statusText = when (status) {
            VehicleStatus.PARKED -> stringResource(R.string.parked_status)
            VehicleStatus.DRIVING -> stringResource(R.string.driving_status)
            VehicleStatus.EXPIRED -> stringResource(R.string.expired_status)
        }

        Text(
            text = statusText,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = detail,
            color = MaterialTheme.colorScheme.surface,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AddCarForm(
    onDismiss: () -> Unit,
    onAddCar: (Vehicle) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var dgtLabel by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add New Vehicle",
                    style = MaterialTheme.typography.titleMedium
                )

                CustomTextField("Name", name) { name = it }
                CustomTextField("Brand", brand) { brand = it }
                CustomTextField("Model", model) { model = it }
                CustomTextField("Year", year) { year = it }
                CustomTextField("Plate", plate) { plate = it }
                CustomTextField("Fuel Type", fuelType) { fuelType = it }
                CustomTextField("DGT Label", dgtLabel) { dgtLabel = it }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && brand.isNotBlank()) {
                                onAddCar(
                                    Vehicle(
                                        name = name,
                                        brand = brand,
                                        model = model,
                                        year = year,
                                        plate = plate,
                                        fuelType = fuelType,
                                        dgtLabel = dgtLabel,
                                        parkingLocation =GeoPoint(0.0, 0.0),
                                        maintenance = null
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onBackground) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
