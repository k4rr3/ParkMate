package com.example.parkmate.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        // Load saved preference
        viewModelScope.launch {
            userPreferences.getDarkMode().collect {
                _isDarkMode.value = it
            }
        }
    }

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue

        // Save new preference
        viewModelScope.launch {
            userPreferences.saveDarkMode(newValue)
        }
    }
    fun getDarkMode(): Boolean {
        Log.d("ThemeViewModel", "getDarkMode called")
        return _isDarkMode.value
    }

}
