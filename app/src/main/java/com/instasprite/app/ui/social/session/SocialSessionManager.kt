package com.instasprite.app.ui.social.session

import com.instasprite.app.data.network.SessionTokenStore
import com.instasprite.app.domain.model.Jwt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialSessionManager @Inject constructor(
    private val tokenStore: SessionTokenStore
) {

    private val _sessionState = MutableStateFlow<SocialSessionState>(SocialSessionState.Unknown)
    val sessionState: StateFlow<SocialSessionState> = _sessionState.asStateFlow()

    init {
        refreshFromStorage()
    }

    fun refreshFromStorage() {
        _sessionState.value = if (tokenStore.isLoggedIn()) {
            SocialSessionState.LoggedIn(tokenStore.getUsername().orEmpty())
        } else {
            SocialSessionState.LoggedOut
        }
    }

    fun onLoginSuccess(jwt: Jwt) {
        tokenStore.saveTokens(
            accessToken = jwt.accessToken,
            refreshToken = jwt.refreshToken,
            tokenType = jwt.type,
            username = jwt.username
        )
        _sessionState.value = SocialSessionState.LoggedIn(jwt.username.orEmpty())
    }

    fun onTokensRefreshed(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        username: String? = null
    ) {
        tokenStore.saveTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            username = username
        )
        _sessionState.value = SocialSessionState.LoggedIn(username.orEmpty())
    }

    fun onLogout() {
        tokenStore.clearTokens()
        _sessionState.value = SocialSessionState.LoggedOut
    }

    fun currentUsername(): String? {
        val state = _sessionState.value as? SocialSessionState.LoggedIn ?: return null
        return state.username.takeIf { it.isNotBlank() }
    }

    fun isLoggedIn(): Boolean = _sessionState.value is SocialSessionState.LoggedIn
}
