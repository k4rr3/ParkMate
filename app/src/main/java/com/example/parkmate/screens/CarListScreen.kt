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
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.mock.*
import com.example.parkmate.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(navController: NavHostController) {
    val vehicles = remember { mutableStateListOf<Vehicle>().apply { addAll(sampleVehicles) } }
    var showAddCarForm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCarForm = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_vehicle)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(vehicles) { vehicle ->
                VehicleCard(navController, vehicle)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showAddCarForm) {
            AddCarForm(
                onDismiss = { showAddCarForm = false },
                onAddCar = { newCar -> vehicles.add(newCar) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(navController: NavHostController, vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { navController.navigate(Screen.CarDetailsScreen.route) }
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
                        text = "${vehicle.make} ${vehicle.model}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = vehicle.licensePlate,
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                StatusIndicator(status = vehicle.status, detail = vehicle.statusDetail)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = vehicle.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            val buttonText = when (vehicle.status) {
                VehicleStatus.EXPIRED -> stringResource(R.string.renew_parking)
                else -> stringResource(R.string.locate_vehicle)
            }

            val buttonColor = when (vehicle.status) {
                VehicleStatus.EXPIRED -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

            val buttonIcon = when (vehicle.status) {
                VehicleStatus.EXPIRED -> Icons.Default.Warning
                else -> Icons.Default.LocationOn
            }

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Icon(imageVector = buttonIcon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, textAlign = TextAlign.Center)
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
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_new_vehicle),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                CustomTextField(stringResource(R.string.name_label), make) { make = it }
                CustomTextField(stringResource(R.string.model_label), model) { model = it }
                CustomTextField(stringResource(R.string.license_plate_label), licensePlate) { licensePlate = it }
                CustomTextField(stringResource(R.string.description_label), description) { description = it }

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (make.isNotBlank() && model.isNotBlank()) {
                                onAddCar(
                                    Vehicle(
                                        make = make,
                                        model = model,
                                        licensePlate = licensePlate,
                                        description = description,
                                        status = VehicleStatus.PARKED,
                                        statusDetail = "Newly created"
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.add_button))
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
