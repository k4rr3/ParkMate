// ui/theme/UserPreference.kt
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
    private val IS_LOGGED_IN_EVER = booleanPreferencesKey("has_logged_in_ever")  // ← NEW

    // === Dark Mode ===
    suspend fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = isDarkMode
        }
    }

    fun getDarkMode(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }

    // === Language ===
    suspend fun saveLanguage(context: Context, lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = lang
        }
    }

    fun getLanguage(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }

    // === Auth State (NEW) ===
    suspend fun setUserHasLoggedIn(context: Context, hasLoggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_EVER] = hasLoggedIn
        }
    }

    fun isUserLoggedInBefore(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[IS_LOGGED_IN_EVER] ?: false
        }
}