package com.instasprite.app.domain.session

sealed interface SocialSessionState {
    data object Unknown : SocialSessionState
    data object LoggedOut : SocialSessionState
    data class LoggedIn(val username: String) : SocialSessionState
}
