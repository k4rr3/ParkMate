package com.example.parkmate.ui.theme

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.N)
class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLang = UserPreference.getLanguage(application).firstOrNull()

            if (savedLang.isNullOrEmpty()) {
                // First launch: use system language and save it
                val sysLang = getSystemLanguage()
                _language.value = sysLang
                UserPreference.saveLanguage(application, sysLang)
            } else {
                // Subsequent launches: use saved language
                _language.value = savedLang
            }
        }
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            UserPreference.saveLanguage(getApplication(), lang)
        }
    }

    private fun getSystemLanguage(): String {
        return getApplication<Application>().resources.configuration.locales[0].language
    }
}
