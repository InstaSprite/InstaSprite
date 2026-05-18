package com.instasprite.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "drawing_screen_settings")

class StorageLocationRepository(
    private val context: Context
) {
    private val LAST_SAVED_LOCATION_KEY = stringPreferencesKey("last_saved_location")

    private val lastSavedLocationFlow = context.dataStore.data.map { preferences ->
        preferences[LAST_SAVED_LOCATION_KEY]?.toUri()
    }

    suspend fun getLastSavedLocation(): Uri? {
        return lastSavedLocationFlow.first()
    }

    suspend fun setLastSavedLocation(uri: Uri) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SAVED_LOCATION_KEY] = uri.toString()
        }
    }

}