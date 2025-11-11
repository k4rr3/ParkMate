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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.LightGreen
import com.example.parkmate.ui.theme.LightOrange
import com.example.parkmate.ui.theme.LightRed
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.Red
import com.example.parkmate.R
import com.example.parkmate.viewmodel.VehicleViewModel
import kotlinx.coroutines.launch


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
                CarInfoCard(vehicle!!) { showEditDialog = true }

                CarRemindersSection()
                AnnualRevisionCard()
                CarInsuranceCard()
                DeleteButton(vehicle!!, navController = navController )

                if (showEditDialog) {
                    EditCarDialog(
                        vehicle = vehicle!!,
                        viewModel = viewModel,
                        onDismiss = { showEditDialog = false }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEditClick(vehicle) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: Icon + Brand / Model / Year
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model} ${vehicle.year}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Fuel: ${vehicle.fuelType} | DGT: ${vehicle.dgtLabel}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    vehicle.maintenance?.let { maintenance ->
                        Text(
                            text = "Maintenance: $maintenance",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom row: License plate + Parking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Plate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = vehicle.plate,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Parking",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (vehicle.parkingLocation.latitude != 0.0 && vehicle.parkingLocation.longitude != 0.0)
                                "Set"
                            else
                                stringResource(R.string.car_not_parked),
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
fun CarRemindersSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Car Reminders",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .background( MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // ITV Inspection Reminder
        ReminderItem(
            icon = Icons.Outlined.Warning,
            iconBackground = LightRed,
            iconTint = Red,
            title = "ITV Inspection",
            dueDate = "Due: March 15, 2024",
            status = "Overdue",
            statusBackground = LightRed,
            statusColor = Red
        )
        
        // Oil Change Reminder
        ReminderItem(
            icon = Icons.Outlined.LocalGasStation,
            iconBackground = LightOrange,
            iconTint = Orange,
            title = "Oil Change",
            dueDate = "Due: April 20, 2024",
            status = "Soon",
            statusBackground = LightOrange,
            statusColor = Orange
        )
        
        // Tire Change Reminder
        ReminderItem(
            icon = Icons.Outlined.TireRepair,
            iconBackground = LightGreen,
            iconTint = Green,
            title = "Tire Change",
            dueDate = "Due: June 10, 2024",
            status = "OK",
            statusBackground = LightGreen,
            statusColor = Green
        )
    }
}

@Composable
fun ReminderItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    dueDate: String,
    status: String,
    statusBackground: Color,
    statusColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = dueDate,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
            }
            
            // Status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusBackground)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AnnualRevisionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Annual Revision",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Book your official car inspection",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Next Revision",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "March 2024",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Pending",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Orange
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Book Official Revision",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CarInsuranceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Car Insurance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Mapfre Insurance Company",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Policy Number",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "POL-2024-X3-001",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Coverage",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Comprehensive",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Expires",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Dec 31, 2024",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Premium",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "€89.99",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "View Policy",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ){
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Visit Mapfre",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteButton(vehicle: Vehicle, viewModel: VehicleViewModel = hiltViewModel(),navController: NavHostController) {
    Button(
        onClick = { viewModel.deleteVehicle(vehicle.id)
            navController.navigate(Screen.CarListScreen.route) {
                popUpTo(Screen.CarListScreen.route) {
                    inclusive = true
                }
            }


        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = stringResource(R.string.delete_vehicle),
            modifier = Modifier.padding(vertical = 8.dp),
            fontSize = 16.sp
        )
    }
}
@Composable
fun EditCarDialog(
    vehicle: Vehicle,
    viewModel: VehicleViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    // Local state for editable fields
    var name by remember { mutableStateOf(vehicle.name ?: "") }
    var brand by remember { mutableStateOf(vehicle.brand) }
    var model by remember { mutableStateOf(vehicle.model) }
    var year by remember { mutableStateOf(vehicle.year) }
    var plate by remember { mutableStateOf(vehicle.plate) }
    var fuelType by remember { mutableStateOf(vehicle.fuelType) }
    var dgtLabel by remember { mutableStateOf(vehicle.dgtLabel) }
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.edit_vehicle)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                CustomTextField("Name", name) { name = it }
                CustomTextField("Brand", brand) { brand = it }
                CustomTextField("Model", model) { model = it }
                CustomTextField("Year", year) { year = it }
                CustomTextField("Plate", plate) { plate = it }
                CustomTextField("Fuel Type", fuelType) { fuelType = it }
                CustomTextField("DGT Label", dgtLabel) { dgtLabel = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    // Validate fields
                    if (brand.isBlank() || model.isBlank() || plate.isBlank()) {
                        errorMessage = context.getString(R.string.Brand_Model_and_Plate_cannot_be_empty)
                        return@TextButton
                    }

                    errorMessage = null
                    isSaving = true

                    // Prepare map for update
                    val updates = mapOf(
                        "name" to name,
                        "brand" to brand,
                        "model" to model,
                        "year" to year,
                        "plate" to plate,
                        "fuelType" to fuelType,
                        "dgtLabel" to dgtLabel
                    )

                    // Firestore update
                    viewModel.viewModelScope.launch {
                        try {
                            viewModel.updateVehicle(vehicle.id, updates)
                            // UI will auto-update because of StateFlow / realtime listener
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = context.getString(R.string.failed_to_save_changes) + " ${e.message}"
                        } finally {
                            isSaving = false
                        }
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}