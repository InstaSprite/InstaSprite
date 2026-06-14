package com.instasprite.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.instasprite.app.data.crypto.PlainSerializer
import com.instasprite.app.data.model.SettingPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.settingsDataStore by dataStore(
    fileName = "settings.pb",
    serializer = PlainSerializer(
        kSerializer = SettingPreferences.serializer(),
        defaultValue = SettingPreferences()
    )
)


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<SettingPreferences> {
        return context.settingsDataStore
    }
}
