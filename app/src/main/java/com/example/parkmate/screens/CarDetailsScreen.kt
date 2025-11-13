package com.example.parkmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.LightGreen
import com.example.parkmate.ui.theme.LightOrange
import com.example.parkmate.ui.theme.LightRed
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.Red
import com.example.parkmate.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    vehicleId: String,
    navController: NavHostController,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val vehicle by viewModel.getVehicleByIdRealtime(vehicleId).collectAsState()

    Scaffold { paddingValues ->
        if (vehicle != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                CarInfoCard(vehicle!!) {
                    viewModel.loadVehicleIntoForm(it) // Pre-fill form before showing dialog
                    showEditDialog = true
                }
                CarRemindersSection()
                AnnualRevisionCard()
                CarInsuranceCard()
                DeleteButton(vehicle!!.id, navController = navController, viewModel = viewModel)

                if (showEditDialog) {
                    EditCarDialog(
                        vehicleId = vehicle!!.id,
                        viewModel = viewModel,
                        onDismiss = {
                            showEditDialog = false
                            viewModel.clearForm() // Clear form state on dismiss
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.car_not_found))
            }
        }
    }
}

@Composable
fun CarInfoCard(vehicle: Vehicle, onEditClick: (Vehicle) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onEditClick(vehicle) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = vehicle.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "${vehicle.brand} ${vehicle.model} · ${vehicle.year}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Plate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(vehicle.plate, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Parking", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (vehicle.parkingLocation.latitude != 0.0) "Set" else "Not Parked",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditCarDialog(
    vehicleId: String,
    viewModel: VehicleViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_vehicle)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ValidatedTextField("Name", formState.name, viewModel::onNameChange, formState.nameError)
                ValidatedTextField("Brand", formState.brand, viewModel::onBrandChange, formState.brandError)
                ValidatedTextField("Model", formState.model, viewModel::onModelChange, formState.modelError)
                ValidatedTextField("Year", formState.year, viewModel::onYearChange, formState.yearError, KeyboardType.Number)
                ValidatedTextField("Plate", formState.plate, viewModel::onPlateChange, formState.plateError)
                ValidatedTextField("Fuel Type", formState.fuelType, viewModel::onFuelTypeChange)
                ValidatedTextField("DGT Label", formState.dgtLabel, viewModel::onDgtLabelChange)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updates = mapOf(
                        "name" to formState.name,
                        "brand" to formState.brand,
                        "model" to formState.model,
                        "year" to formState.year,
                        "plate" to formState.plate,
                        "fuelType" to formState.fuelType,
                        "dgtLabel" to formState.dgtLabel
                    )
                    viewModel.updateVehicle(vehicleId, updates)
                    onDismiss()
                },
                enabled = formState.isFormValid
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
fun DeleteButton(vehicleId: String, viewModel: VehicleViewModel, navController: NavHostController) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to permanently delete this vehicle?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVehicle(vehicleId)
                        showConfirmDialog = false
                        navController.navigate(Screen.CarListScreen.route) {
                            popUpTo(Screen.CarListScreen.route) { inclusive = true }
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Button(
        onClick = { showConfirmDialog = true },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(
            text = stringResource(R.string.delete_vehicle),
            color = MaterialTheme.colorScheme.onError
        )
    }
}


// --- All other Composables from your file (CarRemindersSection, AnnualRevisionCard, etc.) remain unchanged ---
// --- I'm keeping them here so you have the full file ---

@Composable
fun CarRemindersSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Car Reminders", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)).size(32.dp)
            ) {
                Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ReminderItem(Icons.Outlined.Warning, LightRed, Red, "ITV Inspection", "Due: March 15, 2024", "Overdue", LightRed, Red)
        ReminderItem(Icons.Outlined.LocalGasStation, LightOrange, Orange, "Oil Change", "Due: April 20, 2024", "Soon", LightOrange, Orange)
        ReminderItem(Icons.Outlined.TireRepair, LightGreen, Green, "Tire Change", "Due: June 10, 2024", "OK", LightGreen, Green)
    }
}

@Composable
fun ReminderItem(icon: ImageVector, iconBackground: Color, iconTint: Color, title: String, dueDate: String, status: String, statusBackground: Color, statusColor: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(dueDate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(statusBackground).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AnnualRevisionCard() {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Annual Revision", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Book your official car inspection", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Next Revision", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("March 2024", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Pending", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Orange)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                Text("Book Official Revision", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun CarInsuranceCard() {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(LightGreen), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Security, null, tint = Green, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Car Insurance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Mapfre Insurance Company", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Policy Number", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("POL-2024-X3-001", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Coverage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    Text("Comprehensive", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Expires", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dec 31, 2024", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly Premium", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    Text("€89.99", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Description, null, modifier = Modifier.size(16.dp))
                    Text("View Policy", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = { /* TODO */ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                    Text("Visit Mapfre", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
