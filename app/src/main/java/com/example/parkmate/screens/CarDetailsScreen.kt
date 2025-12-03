package com.example.parkmate.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.parkmate.ui.theme.*
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


    LaunchedEffect(vehicleId) {
        viewModel.loadVehicleDetails(vehicleId)
    }

    // Assign the current vehicle to a local variable to avoid race conditions
    val uiState by viewModel.vehicleUiState.collectAsState()
    when {
        // State 1: Show a loading indicator
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // State 2: Show an error message
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.error}")
            }
        }

        // State 3: Show content if vehicle is not null
        uiState.vehicle != null -> {
            val currentVehicle = uiState.vehicle!!
            LaunchedEffect(key1 = currentVehicle.id) {
                viewModel.loadReminders(currentVehicle.id)
            }
            // Pass the loaded vehicle and other states to a content composable
            CarDetailsContent(
                vehicle = uiState.vehicle!!, // We know it's not null here
                navController = navController,
                viewModel = viewModel,
                themeViewModel = themeViewModel
            )
        }

        // State 4: Handle case where loading is done but vehicle is not found
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Vehicle not found")
            }
        }
    }
}

@Composable
fun CarDetailsContent(
    vehicle: Vehicle,
    navController: NavHostController,
    viewModel: VehicleViewModel,
    themeViewModel: ThemeViewModel
) {
    var showEditCarDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditReminderDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<CarReminder?>(null) }

    val reminders by viewModel.reminders.collectAsState()

    Scaffold { paddingValues ->
        // Use the local variable for the check. This is safer.
        if (vehicle != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CarInfoCard(vehicle) {
                        viewModel.loadVehicleIntoForm(it)
                        showEditCarDialog = true
                    }
                }

                item {
                    RemindersHeader(onAddClick = { showAddReminderDialog = true })
                }

                items(reminders, key = { it.id }) { reminder ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReminderItem(
                            reminder = reminder,
                            themeViewModel = themeViewModel,
                            onItemClick = {
                                selectedReminder = it
                                showEditReminderDialog = true
                            }
                        )
                    }
                }

                item { AnnualRevisionCard() }
                item { CarInsuranceCard() }

                // ** THE FIX IS HERE **
                // Only add the DeleteButton to the list if the vehicle is not null
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DeleteButton(
                        vehicleId = vehicle.id,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            // Show a loading indicator while the vehicle is being fetched
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // --- Dialog Management ---
    // The dialogs are outside the main content, so they are safe.
    // They will only be shown when the vehicle is loaded and an item is clicked.

    if (showAddReminderDialog) {
        AddOrEditReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { _, title, dueDate -> // reminderId is null for new reminders
                viewModel.addReminder(vehicle.id, title, dueDate)
                showAddReminderDialog = false
            },
            onDelete = { /* This will not be called in add mode */ }
        )
    }

    if (showEditReminderDialog && selectedReminder != null) {
        AddOrEditReminderDialog(
            initialReminder = selectedReminder,
            onDismiss = {
                showEditReminderDialog = false
                selectedReminder = null
            },
            onSave = { reminderId, title, dueDate ->
                if (reminderId != null) {
                    viewModel.updateReminder(vehicle.id, reminderId, title, dueDate)
                }
                showEditReminderDialog = false
                selectedReminder = null
            },
            onDelete = { reminderId ->
                viewModel.deleteReminder(vehicle.id, reminderId)
            }
        )
    }

    // ** ANOTHER FIX IS HERE **
    // Ensure currentVehicle is not null before trying to show the EditCarDialog
    if (showEditCarDialog && vehicle != null) {
        EditCarDialog(
            vehicleId = vehicle.id,
            viewModel = viewModel,
            onDismiss = {
                showEditCarDialog = false
                viewModel.clearForm()
            }
        )
    }
}


//
// The rest of the file (ReminderItem, AddOrEditReminderDialog, etc.)
// can remain exactly the same as in the previous correct version.
// I am omitting them here for brevity, but they do not need to be changed.
// Just ensure the `CarDetailsScreen` composable above is fully replaced.
//
@Composable
private fun RemindersHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderItem(
    reminder: CarReminder,
    themeViewModel: ThemeViewModel,
    onItemClick: (CarReminder) -> Unit
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val statusColors = getStatusColors(reminder.status, isDarkMode)

    val icon = when (reminder.title.lowercase()) {
        "itv inspection" -> Icons.Outlined.Warning
        "oil change" -> Icons.Outlined.LocalGasStation
        "tire change" -> Icons.Outlined.TireRepair
        else -> Icons.Outlined.Edit
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick(reminder) }, // Simple click to edit
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColors.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColors.content, modifier = Modifier.size(20.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(reminder.title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text("${stringResource(R.string.due_date)}: ${formatDate(reminder.dueDate)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusColors.container)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (reminder.status) {
                        ReminderStatus.PENDING -> stringResource(R.string.pending)
                        ReminderStatus.SOON -> stringResource(R.string.soon)
                        ReminderStatus.OVERDUE -> stringResource(R.string.overdue)
                    },
                    color = statusColors.content,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditReminderDialog(
    initialReminder: CarReminder? = null,
    onDismiss: () -> Unit,onSave: (reminderId: String?, title: String, dueDate: Long) -> Unit,
    onDelete: (reminderId: String) -> Unit
) {
    val isEditMode = initialReminder != null
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // State for the selected date and time. Default to now if creating.
    var selectedDateTime by remember {
        mutableStateOf(
            initialReminder?.let {
                Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
            } ?: LocalDateTime.now()
        )
    }

    // --- Main Dialog ---
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) stringResource(R.string.edit_reminder) else stringResource(R.string.add_reminder)) },
        text = {
            Column {
                // Title field
                ValidatedTextField(
                    label = stringResource(R.string.reminder_title),
                    value = title,
                    onValueChange = { title = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Use our new DateTimePicker composable
                DateTimePicker(
                    initialDateTime = selectedDateTime,
                    onDateTimeSelected = { newDateTime ->
                        selectedDateTime = newDateTime
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(), // Simplified check
                onClick = {
                    val finalTimestamp = selectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onSave(initialReminder?.id, title, finalTimestamp)
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditMode) {
                    TextButton(onClick = { showDeleteConfirmDialog = true }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )

    // --- Delete Confirmation Dialog ---
    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                onDelete(initialReminder!!.id)
                showDeleteConfirmDialog = false
                onDismiss() // Close the main dialog
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true
    )

    // --- Show the Date Picker Dialog ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true // Chain to show time picker next
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // --- Show the Time Picker Dialog ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    // When confirmed, construct the final LocalDateTime and send it back
                    val selectedDate = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    if (selectedDate != null) {
                        val selectedTime = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onDateTimeSelected(LocalDateTime.of(selectedDate, selectedTime))
                    }
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) {
            TimePicker(state = timePickerState, layoutType = TimePickerLayoutType.Vertical)
        }
    }

    // --- The button that starts the process ---
    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        val buttonText = initialDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm"))
        Text(text = buttonText)
    }
}


@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {    AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.confirm_deletion)) },
    text = { Text(stringResource(R.string.are_you_sure_delete_reminder)) },
    confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }
)
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
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
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
