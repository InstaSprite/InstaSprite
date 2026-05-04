package com.olaz.instasprite.data.network

interface SessionTokenStore {
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        username: String? = null
    )

    fun clearTokens()

    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    fun getTokenType(): String

    fun getUsername(): String?

    fun getAuthorizationHeader(): String?

    fun isLoggedIn(): Boolean
}
