package com.olaz.instasprite.data.network

import androidx.datastore.core.DataStore
import com.olaz.instasprite.data.model.TokenPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TokenManager @Inject constructor(private val dataStore: DataStore<TokenPreferences>) {

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        username: String? = null
    ) {
        runBlocking {
            dataStore.updateData { prefs ->
                prefs.copy(
                    keyAccessToken = accessToken,
                    keyRefreshToken = refreshToken,
                    keyTokenType = tokenType,
                    keyUsername = username
                )
            }
        }
    }

    fun clearTokens() {
        runBlocking {
            dataStore.updateData { TokenPreferences() }
        }
    }

    fun getAccessToken(): String? = runBlocking {
        dataStore.data.first().keyAccessToken
    }

    fun getRefreshToken(): String? = runBlocking {
        dataStore.data.first().keyRefreshToken
    }

    fun getTokenType(): String = runBlocking {
        dataStore.data.first().keyTokenType ?: "Bearer"
    }

    fun getUsername(): String? = runBlocking {
        dataStore.data.first().keyUsername
    }

    fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null) "$type $token" else null
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}