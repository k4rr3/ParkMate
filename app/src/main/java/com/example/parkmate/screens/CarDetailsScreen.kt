package com.example.parkmate.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringArrayResource
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
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.text.isNullOrBlank

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



@RequiresApi(Build.VERSION_CODES.O)
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

    // Estado del parking
    val parkingEnd by remember(vehicle.parkingEndTime) {
        derivedStateOf { parseParkingEndTime(vehicle.parkingEndTime) }
    }

    val isCurrentlyParked = parkingEnd?.isAfter(LocalDateTime.now()) == true

    // Cálculo del tiempo restante (se actualiza cada vez que cambia parkingEnd)
    val timeRemaining = remember(parkingEnd, isCurrentlyParked) {
        if (!isCurrentlyParked || parkingEnd == null) null
        else {
            val now = LocalDateTime.now()
            val duration = java.time.Duration.between(now, parkingEnd!!)

            if (duration.isNegative) null
            else {
                val hours = duration.toHours()
                val minutes = duration.toMinutesPart()
                when {
                    hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}min"
                    minutes > 0 -> "${minutes}min"
                    else -> "<1min"
                }
            }
        }
    }

    // Para actualización visual del countdown cada minuto
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isCurrentlyParked) {
        if (isCurrentlyParked) {
            while (true) {
                delay(60_000) // cada minuto
                tick++
            }
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Información principal del vehículo
            item {
                CarInfoCard(vehicle) {
                    viewModel.loadVehicleIntoForm(it)
                    showEditCarDialog = true
                }
            }

            // Estado del aparcamiento
            item {
                ParkingStatusCard(
                    isParked = isCurrentlyParked,
                    endTime = parkingEnd,
                    timeRemaining = timeRemaining,
                    onDispark = {
                        viewModel.updateVehicle(
                            vehicle.id,
                            mapOf(
                                "parkingLocation" to GeoPoint(0.0, 0.0),
                                "parkingEndTime" to null,
                                "activeZoneId" to null
                            ) as Map<String, String>
                        )
                    }
                )
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

            item {
                InsuranceCard(
                    provider = vehicle.insuranceProvider,
                    onSave = { provider -> viewModel.updateInsurance(vehicle.id, provider) },
                    onDelete = { viewModel.deleteInsurance(vehicle.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                DeleteButton(
                    vehicleId = vehicle.id,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    // Diálogos (sin cambios respecto a tu versión original)
    if (showAddReminderDialog) {
        AddOrEditReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { _, title, dueDate ->
                viewModel.addReminder(vehicle.id, title, dueDate)
                showAddReminderDialog = false
            },
            onDelete = { /* no usado aquí */ }
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

    if (showEditCarDialog) {
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

// ──────────────────────────────────────────────────────────────────────────────
// Tarjeta de estado de aparcamiento
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ParkingStatusCard(
    isParked: Boolean,
    endTime: LocalDateTime?,
    timeRemaining: String?,
    onDispark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isParked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalParking,
                    contentDescription = null,
                    tint = if (isParked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )

                Column {
                    Text(
                        text = if (isParked) "Currently parked" else "Not parked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (isParked && endTime != null) {
                        Text(
                            text = "Ends: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm '·' dd/MM"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isParked && timeRemaining != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Time remaining: $timeRemaining",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onDispark,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unpark now")
                }
            } else if (!isParked) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "You can park this vehicle from the map",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Función auxiliar para parsear el campo parkingEndTime
// Ajusta el formato según lo que realmente guardes en Firestore
// ──────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
private fun parseParkingEndTime(timeString: String?): LocalDateTime? {
    if (timeString.isNullOrBlank()) return null

    return try {
        // Formato ISO más común y recomendado
        LocalDateTime.parse(timeString)

        // Alternativas si usas otro formato:
        // DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(timeString, LocalDateTime::from)
        // DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").parse(timeString, LocalDateTime::from)
    } catch (e: Exception) {
        null
    }
}


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
                ValidatedTextField("Year", formState.year, viewModel::onYearChange, formState.yearError?.let { stringResource(id = it) }, KeyboardType.Number)
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
    // 1. Get the UriHandler to open links
    val uriHandler = LocalUriHandler.current
    val revisionUrl = "https://www.applusiteuve.com/ca-es/"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(stringResource(R.string.annual_revision), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        stringResource(R.string.annual_revision_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { uriHandler.openUri(revisionUrl) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null, // Decorative icon
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.book_official_revision),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceCard(
    provider: String?,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val companyNames = stringArrayResource(id = R.array.insurance_companies_array)
    val companyUrls = stringArrayResource(id = R.array.insurance_company_urls_array)
    val companyUrlMap = remember { companyNames.zip(companyUrls).toMap() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.car_insurance), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (provider.isNullOrBlank()) {
                // Show "Add Insurance" button
                Button(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_insurance))
                }
            } else {
                // Show insurance details
                Text(text = stringResource(R.string.insurance_provider), style = MaterialTheme.typography.bodySmall)
                Text(text = provider, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val urlToVisit = companyUrlMap[provider]
                    if (!urlToVisit.isNullOrBlank()) {
                        Button(onClick = { uriHandler.openUri(urlToVisit) }) {
                            Text(stringResource(R.string.visit_policy_webpage))
                        }
                    }

                    Row {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_insurance))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        InsuranceEditDialog(
            initialProvider = provider,
            onDismiss = { showEditDialog = false },
            onSave = { newProvider ->
                onSave(newProvider)
                showEditDialog = false
            },
            companyList = companyNames
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceEditDialog(
    initialProvider: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    companyList: Array<String>
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf(initialProvider ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(if (initialProvider == null) R.string.add_insurance else R.string.edit_insurance)) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.insurance_provider)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        companyList.forEach { company ->
                            DropdownMenuItem(
                                text = { Text(company) },
                                onClick = {
                                    selectedProvider = company
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedProvider) },
                enabled = selectedProvider.isNotBlank()
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

