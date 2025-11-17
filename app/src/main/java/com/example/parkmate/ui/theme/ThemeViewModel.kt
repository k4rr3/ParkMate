package com.example.parkmate.ui.theme

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        // Load saved preference
        viewModelScope.launch {
            UserPreference.getDarkMode(application).collect {
                _isDarkMode.value = it
            }
        }
    }

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue

        // Save new preference
        viewModelScope.launch {
            UserPreference.saveDarkMode(this@ThemeViewModel.getApplication(), newValue)
        }
    }
    fun getDarkMode(): Boolean {
        Log.d("ThemeViewModel", "getDarkMode called")
        return _isDarkMode.value
    }

}
