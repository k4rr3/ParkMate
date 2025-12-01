package com.example.parkmate.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.data.models.CarReminder
import com.example.parkmate.data.models.ReminderStatus
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.LightGreen
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.ThemeViewModel
import com.example.parkmate.ui.theme.getStatusColors
import com.example.parkmate.viewmodel.VehicleViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    vehicleId: String,
    navController: NavHostController,
    viewModel: VehicleViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    val vehicle by viewModel.getVehicleByIdRealtime(vehicleId).collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    LaunchedEffect(vehicleId) {
        viewModel.loadReminders(vehicleId)
    }

    Scaffold { paddingValues ->
        if (vehicle != null) {
            // Use LazyColumn as the main layout for the entire screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Adds space between items
            ) {
                // Item 1: Car Info Card
                item {
                    CarInfoCard(vehicle!!) {
                        viewModel.loadVehicleIntoForm(it)
                        showEditDialog = true
                    }
                }

                // Item 2: Reminders Header
                item {
                    RemindersHeader(onAddClick = { showAddReminderDialog = true })
                }

                // Items 3...N: The list of reminders
                items(reminders) { reminder ->
                    // Add horizontal padding to match the rest of the screen
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReminderItem(reminder = reminder,themeViewModel)
                    }
                }

                // Item N+1: Annual Revision Card
                item {
                    AnnualRevisionCard()
                }

                // Item N+2: Car Insurance Card
                item {
                    CarInsuranceCard()
                }

                // Item N+3: Delete Button (with extra padding at the top)
                item {
                    Spacer(modifier = Modifier.height(8.dp)) // Add some space before the button
                    DeleteButton(vehicle!!.id, navController = navController, viewModel = viewModel)
                }
            }
        } else {
            // This part remains the same
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.car_not_found))
            }
        }
    }

    // --- Dialogs remain unchanged ---
    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { title, dueDate ->
                viewModel.addReminder(vehicleId, title, dueDate)
                showAddReminderDialog = false
            }
        )
    }

    if (showEditDialog && vehicle != null) {
        EditCarDialog(
            vehicleId = vehicle!!.id,
            viewModel = viewModel,
            onDismiss = {
                showEditDialog = false
                viewModel.clearForm()
            }
        )
    }
}

// New helper composable for the header
@Composable
private fun RemindersHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Added vertical padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.car_reminders), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                .size(32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_reminder), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(
            text = stringResource(R.string.delete_vehicle),
            color = MaterialTheme.colorScheme.onError
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderItem(reminder: CarReminder,themeViewModel: ThemeViewModel) {
    // Get the correct theme-aware colors for the status
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val statusColors = getStatusColors(reminder.status,isDarkMode)

    val icon = when (reminder.title.lowercase()) {
        "itv inspection" -> Icons.Outlined.Warning
        "oil change" -> Icons.Outlined.LocalGasStation
        "tire change" -> Icons.Outlined.TireRepair
        else -> Icons.Outlined.Edit
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with themed background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColors.container), // Use custom container color
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColors.content, modifier = Modifier.size(20.dp)) // Use custom content color
            }

            // Title and due date
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(reminder.title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                // Assuming formatDate is defined elsewhere
                Text("${stringResource(R.string.due_date)}: ${formatDate(reminder.dueDate)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusColors.container) // Use custom container color
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (reminder.status) {
                        ReminderStatus.PENDING -> stringResource(R.string.pending)
                        ReminderStatus.SOON -> stringResource(R.string.soon)
                        ReminderStatus.OVERDUE -> stringResource(R.string.overdue)
                    },
                    color = statusColors.content, // Use custom content color
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun AnnualRevisionCard() {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)) {
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
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightGreen), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Security, null, tint = Green, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)) {
                    Text("Car Insurance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Mapfre Insurance Company", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.policy_number), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("POL-2024-X3-001", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.coverage), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    Text("Comprehensive", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.expires), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dec 31, 2024", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.monthly_remium), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    Text("€89.99", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Description, null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.view_policy), modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = { /* TODO */ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.visit_mapfre), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    // Create and remember the DatePicker state
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_date)) },
        text = {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        onDateSelected(Date(selectedMillis))
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Initialize with the current date and time to be user-friendly
    val initialDateTime = LocalDateTime.now()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true
    )

    // This will hold the final combined date and time
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }


    // --- Dialog for Date Selection ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // IMPORTANT: Show the time picker right after the date is picked
                        showTimePicker = true
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Dialog for Time Selection ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {                    showTimePicker = false
                        // --- Combine Date and Time ---
                        val selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        if (selectedDate != null) {
                            // THE FIX IS HERE: Use the standard java.time.LocalTime
                            val selectedTime = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                            selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            TimePicker(state = timePickerState, layoutType = TimePickerLayoutType.Vertical)
        }
    }


    // --- Main Add Reminder Dialog ---
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_reminder)) },
        text = {
            Column {
                ValidatedTextField(
                    label = stringResource(R.string.reminder_title),
                    value = title,
                    onValueChange = { title = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger the date/time selection flow
                OutlinedButton(
                    onClick = { showDatePicker = true }, // Start with the date picker
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val buttonText = selectedDateTime?.let {
                        // Format the combined date and time for display
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")
                        it.format(formatter)
                    } ?: stringResource(R.string.pick_date)

                    Text(text = buttonText)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && selectedDateTime != null,
                onClick = {
                    // Convert the final LocalDateTime to a Long timestamp for saving
                    val finalTimestamp = selectedDateTime!!
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    onSave(title, finalTimestamp)
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// Helper composable to wrap the TimePicker in a Dialog
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(date: Any?): String {
    if (date == null) return ""
    var pattern = "dd/MM/yyyy"
    return try {
        when (date) {
            is LocalDate -> {
                // Format LocalDate to dd/MM/yyyy
                val formatter = DateTimeFormatter.ofPattern(pattern)
                date.format(formatter)
            }

            is Date -> {
                // Firestore stores date as java.util.Date
                val instant = date.toInstant()
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern(pattern)
                localDate.format(formatter)
            }

            is Long -> {
                // unix timestamp in milliseconds
                val instant = Instant.ofEpochMilli(date)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern(pattern)
                localDate.format(formatter)
            }

            is String -> {
                // Already formatted or Firestore string fallback
                date
            }

            else -> ""
        }
    } catch (e: Exception) {
        ""
    }
}