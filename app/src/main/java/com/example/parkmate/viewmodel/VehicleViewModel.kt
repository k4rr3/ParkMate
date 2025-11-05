package com.example.parkmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.Vehicle
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let { uid ->
                try {
                    // 1️⃣ Add vehicle to Firestore
                    val vehicleId = firestoreRepository.addVehicle(vehicle.copy())

                    // 2️⃣ Attach vehicle to the user
                    val user = firestoreRepository.getUser(uid)
                    if (user != null) {
                        val updatedVehicleIds = (user.vehicleID ?: emptyList()) + vehicleId
                        firestoreRepository.updateUser(uid, mapOf("vehicleID" to updatedVehicleIds))
                    }

                    // 3️⃣ Refresh vehicles
                    loadUserVehicles()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateVehicle(vehicleId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                firestoreRepository.updateVehicle(vehicleId, updates)
                loadUserVehicles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let { uid ->
                try {
                    firestoreRepository.deleteVehicle(vehicleId)

                    // Remove the vehicle ID from user doc
                    val user = firestoreRepository.getUser(uid)
                    if (user != null) {
                        val updatedVehicleIds = (user.vehicleID ?: emptyList()).filter { it != vehicleId }
                        firestoreRepository.updateUser(uid, mapOf("vehicleID" to updatedVehicleIds))
                    }

                    loadUserVehicles()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
