package com.example.parkmate.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun loadUserVehicles() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let {
                _vehicles.value = firestoreRepository.getUserVehicles(it)
            }
        }
    }

    /**
     * 🔹 Returns a Flow<Vehicle?> for a specific vehicle ID.
     * This allows Compose to observe changes reactively.
     */
    fun getVehicleByIdRealtime(vehicleId: String): StateFlow<Vehicle?> {
        val state = MutableStateFlow<Vehicle?>(null)
        firestoreRepository.getVehicleRealtime(vehicleId) { vehicle ->
            state.value = vehicle
        }
        return state
    }


    /**
     * 🔹 Add a new vehicle to Firestore and link it to the current user.
     */
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let { uid ->
                try {
                    // 1️⃣ Add vehicle to Firestore
                    val vehicleId = firestoreRepository.addVehicle(vehicle.copy())

                    // 2️⃣ Link vehicle to user
                    val user = firestoreRepository.getUser(uid)
                    if (user != null) {
                        val updatedVehicleIds = (user.vehicleID ?: emptyList()) + vehicleId
                        firestoreRepository.updateUser(uid, mapOf("vehicleID" to updatedVehicleIds))
                    }

                    // 3️⃣ Refresh user vehicles
                    loadUserVehicles()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 🔹 Update a vehicle’s Firestore data.
     */
    fun updateVehicle(vehicleId: String, updates: Map<String, String>) {
        viewModelScope.launch {
            try {
                firestoreRepository.updateVehicle(vehicleId, updates)
                loadUserVehicles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 🔹 Delete a vehicle and remove it from the user’s vehicle list.
     */
    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let { uid ->
                try {
                    firestoreRepository.deleteVehicle(vehicleId)

                    val user = firestoreRepository.getUser(uid)
                    if (user != null) {
                        val updatedVehicleIds =
                            (user.vehicleID ?: emptyList()).filter { it != vehicleId }
                        firestoreRepository.updateUser(uid, mapOf("vehicleID" to updatedVehicleIds))
                    }

                    loadUserVehicles()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Data class to hold the state of the vehicle form.
    data class VehicleFormState(
        val name: String = "",
        val brand: String = "",
        val model: String = "",
        val year: String = "",
        val plate: String = "",
        val fuelType: String = "",
        val dgtLabel: String = "",
        val nameError: String? = null,
        val brandError: String? = null,
        val modelError: String? = null,
        val yearError: String? = null,
        val plateError: String? = null,
        val dgtLabelError: String? = null,
        val isFormValid: Boolean = false
    )

    private val _formState = MutableStateFlow(VehicleFormState())
    val formState: StateFlow<VehicleFormState> = _formState.asStateFlow()

    private val plateRegex = Regex("^[0-9]{4}[BCDFGHJKLMNPQRSTVWXYZ]{3}$")
    private val yearRegex = Regex("^[0-9]{4}$")
    private val generalTextRegex = Regex("^[A-Za-z0-9\\s]{2,}$")

    private fun validatePlate(plate: String): String? {
        if (plate.isBlank()) return "Plate is required."
        return if (!plate.matches(plateRegex)) "Invalid format (e.g., 1234ABC)" else null
    }

    private fun validateYear(year: String): String? {
        if (year.isBlank()) return "Year is required."
        if (!year.matches(yearRegex)) return "Must be a 4-digit year."
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val intYear = year.toIntOrNull() ?: return "Invalid year."
        return if (intYear < 1900 || intYear > currentYear) "Year must be between 1900 and $currentYear" else null
    }

    private fun validateGeneralText(text: String, fieldName: String): String? {
        if (text.isBlank()) return "$fieldName is required."
        return if (!text.matches(generalTextRegex)) "$fieldName must be at least 2 characters" else null
    }
    // In VehicleViewModel.kt

    private fun validateDgtLabel(dgtLabel: String): String? {
        val validLabels = listOf("0", "ECO", "C", "B")
        if (dgtLabel.isBlank()) {
            return "DGT Label is required."
        }
        if (dgtLabel.uppercase() !in validLabels) {
            return "Invalid label. Must be 0, ECO, C, or B."
        }
        return null
    }


    fun validateForm() {
        val state = _formState.value
        val nameError = validateGeneralText(state.name, "Name")
        val brandError = validateGeneralText(state.brand, "Brand")
        val modelError = validateGeneralText(state.model, "Model")
        val yearError = validateYear(state.year)
        val plateError = validatePlate(state.plate)
        val dgtLabelError = validateDgtLabel(state.dgtLabel)

        _formState.update {
            it.copy(
                nameError = nameError,
                brandError = brandError,
                modelError = modelError,
                yearError = yearError,
                plateError = plateError,
                dgtLabelError = dgtLabelError,
                isFormValid = nameError == null && brandError == null && modelError == null &&
                        yearError == null && plateError == null && dgtLabelError == null // Add dgtLabelError here
            )
        }
    }

    // Public functions called from the UI
    fun onNameChange(value: String) {
        _formState.update { it.copy(name = value) }; validateForm()
    }

    fun onBrandChange(value: String) {
        _formState.update { it.copy(brand = value) }; validateForm()
    }

    fun onModelChange(value: String) {
        _formState.update { it.copy(model = value) }; validateForm()
    }

    fun onYearChange(value: String) {
        _formState.update { it.copy(year = value) }; validateForm()
    }

    fun onPlateChange(value: String) {
        _formState.update { it.copy(plate = value.uppercase()) }; validateForm()
    }

    fun onFuelTypeChange(value: String) {
        _formState.update { it.copy(fuelType = value) }
    }

    fun onDgtLabelChange(value: String) {
        _formState.update { it.copy(dgtLabel = value) }; validateForm()
    }

    // Used to pre-fill the form for editing
    fun loadVehicleIntoForm(vehicle: Vehicle) {
        _formState.value = VehicleFormState(
            name = vehicle.name,
            brand = vehicle.brand,
            model = vehicle.model,
            year = vehicle.year,
            plate = vehicle.plate,
            fuelType = vehicle.fuelType,
            dgtLabel = vehicle.dgtLabel
        )
        validateForm() // Validate immediately
    }

    // Used to clear the form
    fun clearForm() {
        _formState.value = VehicleFormState()
    }
}