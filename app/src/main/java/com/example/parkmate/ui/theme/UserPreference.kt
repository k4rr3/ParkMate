package com.example.parkmate.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

object UserPreference {

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")


    // Save dark mode preference
    suspend fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = isDarkMode
        }
    }

    // Get dark mode preference as a Flow
    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY] ?: false // default = light mode
        }
    }
    suspend fun saveLanguage(context: Context, lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = lang
        }
    }

    /** Get app language preference as Flow<String> */
    fun getLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: "en" // default = English
        }
    }
}
