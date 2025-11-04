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

    fun loadUserVehicles() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            userId?.let {
                _vehicles.value = firestoreRepository.getUserVehicles(it)
            }
        }
    }

    // Get current user ID directly
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Add other vehicle operations...
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            userId?.let {
                try {
                    val vehicleId = firestoreRepository.addVehicle(vehicle)
                    // Reload vehicles after adding
                    loadUserVehicles()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun updateVehicle(vehicleId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                firestoreRepository.updateVehicle(vehicleId, updates)
                // Reload vehicles after updating
                loadUserVehicles()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.deleteVehicle(vehicleId)
                // Reload vehicles after deleting
                loadUserVehicles()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}