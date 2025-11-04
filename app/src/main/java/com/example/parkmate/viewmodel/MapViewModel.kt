package com.example.parkmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.parkmate.data.models.InterestPoint
import com.example.parkmate.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// presentation/viewmodel/MapViewModel.kt
@HiltViewModel
class MapViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _gasStations = MutableStateFlow<List<InterestPoint>>(emptyList())
    val gasStations: StateFlow<List<InterestPoint>> = _gasStations.asStateFlow()

    private val _mechanics = MutableStateFlow<List<InterestPoint>>(emptyList())
    val mechanics: StateFlow<List<InterestPoint>> = _mechanics.asStateFlow()

    fun loadNearbyGasStations(lat: Double, lon: Double, radius: Double = 5.0) {
        firestoreRepository.getNearbyGasStationsRealTime(lat, lon, radius) { points ->
            _gasStations.value = points
        }
    }

    suspend fun loadMechanics(): List<InterestPoint> {
        return firestoreRepository.getMechanics()
    }
}