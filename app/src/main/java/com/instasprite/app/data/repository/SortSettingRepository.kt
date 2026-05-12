package com.instasprite.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.instasprite.app.ui.gallery.SpriteListOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "home_screen_settings")

class SortSettingRepository(
    private val context: Context
) {
    private val LAST_SORT_SETTING_KEY = stringPreferencesKey("last_sort_setting")

    private val lastSortSettingFlow = context.dataStore.data.map { preferences ->
        preferences[LAST_SORT_SETTING_KEY]
    }

    suspend fun getLastSortSetting(): SpriteListOrder? {
        return lastSortSettingFlow.first()?.let { SpriteListOrder.valueOf(it) }
    }

    suspend fun setLastSortSetting(spriteListOrder: SpriteListOrder) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SORT_SETTING_KEY] = spriteListOrder.toString()
        }
    }
}