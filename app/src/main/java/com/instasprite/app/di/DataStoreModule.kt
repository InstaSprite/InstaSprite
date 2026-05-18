package com.instasprite.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.instasprite.app.data.crypto.EncryptedSerializer
import com.instasprite.app.data.model.AccountMapPreferences
import com.instasprite.app.data.model.SettingPreferences
import com.instasprite.app.data.model.TokenPreferences
import com.instasprite.app.data.network.SessionTokenStore
import com.instasprite.app.data.network.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.tokenDataStore by dataStore(
    fileName = "token_prefs.pb",
    serializer = EncryptedSerializer(
        kSerializer = TokenPreferences.serializer(),
        defaultValue = TokenPreferences()
    )
)

val Context.settingsDataStore by dataStore(
    fileName = "settings.pb",
    serializer = EncryptedSerializer(
        kSerializer = SettingPreferences.serializer(),
        defaultValue = SettingPreferences()
    )
)

val Context.accountsDataStore by dataStore(
    fileName = "accounts.pb",
    serializer = EncryptedSerializer(
        kSerializer = AccountMapPreferences.serializer(),
        defaultValue = AccountMapPreferences()
    )
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(@ApplicationContext context: Context): DataStore<TokenPreferences> {
        return context.tokenDataStore
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<SettingPreferences> {
        return context.settingsDataStore
    }

    @Provides
    @Singleton
    fun provideAccountsDataStore(@ApplicationContext context: Context): DataStore<AccountMapPreferences> {
        return context.accountsDataStore
    }

    @Provides
    @Singleton
    fun provideTokenUtils(dataStore: DataStore<TokenPreferences>): TokenManager {
        return TokenManager(dataStore)
    }

    @Provides
    @Singleton
    fun provideSessionTokenStore(tokenManager: TokenManager): SessionTokenStore {
        return tokenManager
    }
}
