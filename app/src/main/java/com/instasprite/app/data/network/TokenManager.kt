package com.instasprite.app.data.network

import androidx.datastore.core.DataStore
import com.instasprite.app.data.model.TokenPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TokenManager @Inject constructor(
    private val dataStore: DataStore<TokenPreferences>
) : SessionTokenStore {

    @Volatile
    private var cachedTokens: TokenPreferences? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Hydrate cache synchronously on first access, but observe changes
        scope.launch {
            dataStore.data.collect { prefs ->
                cachedTokens = prefs
            }
        }
    }

    private fun getPrefs(): TokenPreferences {
        var current = cachedTokens
        if (current == null) {
            current = runBlocking { dataStore.data.first() }
            cachedTokens = current
        }
        return current
    }

    override fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        username: String?
    ) {
        val finalUsername = if (!username.isNullOrEmpty()) username else getUsername()
        cachedTokens = TokenPreferences(
            keyAccessToken = accessToken,
            keyRefreshToken = refreshToken,
            keyTokenType = tokenType,
            keyUsername = finalUsername
        )
        scope.launch {
            dataStore.updateData {
                it.copy(
                    keyAccessToken = accessToken,
                    keyRefreshToken = refreshToken,
                    keyTokenType = tokenType,
                    keyUsername = finalUsername
                )
            }
        }
    }

    override fun clearTokens() {
        cachedTokens = TokenPreferences()
        scope.launch {
            dataStore.updateData { TokenPreferences() }
        }
    }

    override fun getAccessToken(): String? = getPrefs().keyAccessToken

    override fun getRefreshToken(): String? = getPrefs().keyRefreshToken

    override fun getTokenType(): String = getPrefs().keyTokenType ?: "Bearer"

    override fun getUsername(): String? = getPrefs().keyUsername

    override fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null) "$type $token" else null
    }

    override fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
