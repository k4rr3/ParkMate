package com.example.parkmate.ui.theme

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.parkmate.data.preferences.UserPreferences
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLang = userPreferences.getLanguage().firstOrNull()

            if (savedLang.isNullOrEmpty()) {
                // First launch: use system language and save it
                val sysLang = getSystemLanguage()
                _language.value = sysLang
                userPreferences.saveLanguage(sysLang)
            } else {
                // Subsequent launches: use saved language
                _language.value = savedLang
            }
        }
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            userPreferences.saveLanguage(lang)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getSystemLanguage(): String {
        return Locale.getDefault().language  // Modern, clean way
    }
}
