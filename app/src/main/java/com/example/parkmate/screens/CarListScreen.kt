package com.example.parkmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.ui.theme.CarListIcon
import com.example.parkmate.viewmodel.VehicleViewModel
import com.google.firebase.firestore.GeoPoint
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import com.example.parkmate.R

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
                Text(stringResource(R.string.no_vehicles_yet_add_one))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleCard(navController, vehicle)
                }
            }
        }

        if (showAddCarForm) {
            AddCarForm(
                onDismiss = {
                    showAddCarForm = false
                    viewModel.clearForm() // Clear form state when dialog is dismissed
                },
                viewModel = viewModel // Pass the existing ViewModel instance
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(navController: NavHostController, vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { navController.navigate(Screen.CarDetailsScreen.withVehicleId(vehicle.id)) }
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
                        text = vehicle.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = vehicle.plate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${vehicle.brand} ${vehicle.model}  ·  ${vehicle.year}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Screen.CarDetailsScreen.withVehicleId(vehicle.id)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.view_details), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun AddCarForm(
    onDismiss: () -> Unit,
    viewModel: VehicleViewModel
) {
    val formState by viewModel.formState.collectAsState()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_new_vehicle),
                    style = MaterialTheme.typography.titleLarge
                )

                ValidatedTextField(
                    label = stringResource(R.string.name),
                    value = formState.name,
                    onValueChange = viewModel::onNameChange,
                    errorMessage = formState.nameError
                )

                ValidatedTextField(
                    label = stringResource(R.string.brand),
                    value = formState.brand,
                    onValueChange = viewModel::onBrandChange,
                    errorMessage = formState.brandError
                )

                ValidatedTextField(
                    label = stringResource(R.string.model),
                    value = formState.model,
                    onValueChange = viewModel::onModelChange,
                    errorMessage = formState.modelError
                )

                ValidatedTextField(
                    label = stringResource(R.string.year),
                    value = formState.year,
                    onValueChange = viewModel::onYearChange,
                    errorMessage = formState.yearError,
                    keyboardType = KeyboardType.Number
                )

                ValidatedTextField(
                    label = stringResource(R.string.plate),
                    value = formState.plate,
                    onValueChange = viewModel::onPlateChange,
                    errorMessage = formState.plateError
                )

                ValidatedTextField(
                    label = stringResource(R.string.fuel_type),
                    value = formState.fuelType,
                    onValueChange = viewModel::onFuelTypeChange
                )

                ValidatedTextField(
                    label = stringResource(R.string.dgt_label),
                    value = formState.dgtLabel,
                    onValueChange = viewModel::onDgtLabelChange,
                    errorMessage = formState.dgtLabelError
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val newVehicle = Vehicle(
                                name = formState.name,
                                brand = formState.brand,
                                model = formState.model,
                                year = formState.year,
                                plate = formState.plate,
                                fuelType = formState.fuelType,
                                dgtLabel = formState.dgtLabel,
                                parkingLocation = GeoPoint(0.0, 0.0),
                                maintenance = null
                            )
                            viewModel.addVehicle(newVehicle)
                            onDismiss()
                        },
                        enabled = formState.isFormValid
                    ) {
                        Text(stringResource(R.string.add))
                    }
                }
            }
        }
    }
}


// Reusable validated text field to avoid repeating code
@Composable
fun ValidatedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = errorMessage != null,
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
