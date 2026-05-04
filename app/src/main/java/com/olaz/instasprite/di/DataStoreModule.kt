package com.olaz.instasprite.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.olaz.instasprite.data.crypto.EncryptedSerializer
import com.olaz.instasprite.data.model.AccountMapPreferences
import com.olaz.instasprite.data.model.SettingPreferences
import com.olaz.instasprite.data.model.TokenPreferences
import com.olaz.instasprite.data.network.SessionTokenStore
import com.olaz.instasprite.data.network.TokenManager
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
