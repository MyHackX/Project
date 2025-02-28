package com.example.myhackx.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    
    private val dataStore = context.dataStore

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val IS_ADMIN = booleanPreferencesKey("is_admin")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setAdmin(isAdmin: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ADMIN] = isAdmin
        }
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val isAdmin: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_ADMIN] ?: false
    }

    val userEmail: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL] ?: ""
    }
} 