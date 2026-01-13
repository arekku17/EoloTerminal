package com.argento.eoloapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(context: Context) {

    private val dataStore = context.dataStore

    val authToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_TOKEN]
        }

    val userId: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_USER_ID]
        }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }
}