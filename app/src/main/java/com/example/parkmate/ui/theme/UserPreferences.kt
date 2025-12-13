// data/preferences/UserPreferencesManager.kt
package com.example.parkmate.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
        val IS_LOGGED_IN_EVER = booleanPreferencesKey("has_logged_in_ever")
    }

    suspend fun saveDarkMode(isDarkMode: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = isDarkMode }
    }

    fun getDarkMode(): Flow<Boolean> =
        dataStore.data.map { it[Keys.DARK_MODE] ?: false }

    suspend fun saveLanguage(lang: String) {
        dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    fun getLanguage(): Flow<String> =
        dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    suspend fun setUserHasLoggedIn(hasLoggedIn: Boolean) {
        dataStore.edit { it[Keys.IS_LOGGED_IN_EVER] = hasLoggedIn }
    }

    fun isUserLoggedInBefore(): Flow<Boolean> =
        dataStore.data.map { it[Keys.IS_LOGGED_IN_EVER] ?: false }
}