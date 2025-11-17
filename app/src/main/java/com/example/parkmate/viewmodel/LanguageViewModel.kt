package com.example.parkmate.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            UserPreference.getLanguage(application).collect {
                _language.value = it
            }
        }
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            UserPreference.saveLanguage(getApplication(), lang)
        }
    }
}
