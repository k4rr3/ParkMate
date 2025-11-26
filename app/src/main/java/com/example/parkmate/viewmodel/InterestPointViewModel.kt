package com.example.parkmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.models.InterestPoint
import com.example.parkmate.data.repository.FirestoreRepository // <-- ¡USA TU REPOSITORIO!
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestPointViewModel @Inject constructor(
    private val repository: FirestoreRepository // <-- Inyectamos tu repositorio existente
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _interestPoints = MutableStateFlow<List<InterestPoint>>(emptyList())
    val interestPoints: StateFlow<List<InterestPoint>> = _interestPoints.asStateFlow()

    init {
        // Al iniciar, cargamos todos los puntos de interés.
        // Podríamos tener funciones separadas si quisiéramos cargar solo gasolineras o solo mecánicos.
        loadAllInterestPoints()
    }

    private fun loadAllInterestPoints() {
        viewModelScope.launch {
            _isLoading.value = true
            // Llamamos a las funciones que YA existen en tu FirestoreRepository
            val gasStations = repository.getGasStations()
            val mechanics = repository.getMechanics()
            _interestPoints.value = gasStations + mechanics // Los unimos en una sola lista
            _isLoading.value = false
        }
    }
}
