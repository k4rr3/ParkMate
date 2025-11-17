package com.example.parkmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.Zone
import com.example.parkmate.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZoneViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    val zones: StateFlow<List<Zone>> = _zones.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    init {
        fetchAllZones()
    }

    fun fetchAllZones() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedZones = repository.getZones()
                // --- LOG PARA VER SI FIREBASE DEVUELVE ALGO ---
                Log.d("DEBUG_ZVM", "Zonas obtenidas de Firestore: ${fetchedZones.size}")
                if (fetchedZones.isNotEmpty()) {
                    fetchedZones.forEach { zone ->
                        Log.d("DEBUG_ZVM", " -> Nombre: ${zone.name}, Puntos: ${zone.vector.size}")
                    }
                }
                // ---------------------------------------------
                _zones.value = fetchedZones
            } catch (e: Exception) {
                Log.e("DEBUG_ZVM", "Error al obtener zonas: ${e.message}", e)
                _resultMessage.value = "Error fetching zones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addZone(zone: Zone) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addZone(zone)
                _resultMessage.value = "Zone added successfully."
                fetchAllZones()
            } catch (e: Exception) {
                _resultMessage.value = "Error adding zone: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateZone(zoneId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateZone(zoneId, updates)
                _resultMessage.value = "Zone updated successfully."
                fetchAllZones()
            } catch (e: Exception) {
                _resultMessage.value = "Error updating zone: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteZone(zoneId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteZone(zoneId)
                _resultMessage.value = "Zone deleted successfully."
                fetchAllZones()
            } catch (e: Exception) {
                _resultMessage.value = "Error deleting zone: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResultMessage() {
        _resultMessage.value = null
    }
}
