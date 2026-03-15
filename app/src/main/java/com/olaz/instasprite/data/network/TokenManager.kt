package com.olaz.instasprite.data.network

import androidx.datastore.core.DataStore
import com.olaz.instasprite.data.model.TokenPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val dataStore: DataStore<TokenPreferences>
) : SessionTokenStore {

    override fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        username: String?
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

    override fun clearTokens() {
        runBlocking {
            dataStore.updateData { TokenPreferences() }
        }
    }

    override fun getAccessToken(): String? = runBlocking {
        dataStore.data.first().keyAccessToken
    }

    override fun getRefreshToken(): String? = runBlocking {
        dataStore.data.first().keyRefreshToken
    }

    override fun getTokenType(): String = runBlocking {
        dataStore.data.first().keyTokenType ?: "Bearer"
    }

    override fun getUsername(): String? = runBlocking {
        dataStore.data.first().keyUsername
    }

    override fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null) "$type $token" else null
    }

    override fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
