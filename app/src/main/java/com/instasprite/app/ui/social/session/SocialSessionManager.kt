package com.instasprite.app.ui.social.session

import com.instasprite.app.data.network.SessionTokenStore
import com.instasprite.app.data.repository.AccountRepository
import com.instasprite.app.data.repository.ProfileRepository
import com.instasprite.app.domain.model.Jwt
import com.instasprite.app.utils.Constants
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialSessionManager @Inject constructor(
    private val tokenStore: SessionTokenStore,
    private val profileRepository: Lazy<ProfileRepository>,
    private val accountRepository: AccountRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionState = MutableStateFlow<SocialSessionState>(SocialSessionState.Unknown)
    val sessionState: StateFlow<SocialSessionState> = _sessionState.asStateFlow()

    private val _currentUser = MutableStateFlow<CurrentUserState?>(null)
    val currentUser: StateFlow<CurrentUserState?> = _currentUser.asStateFlow()

    init {
        refreshFromStorage()
    }

    fun refreshFromStorage() {
        val isLoggedIn = tokenStore.isLoggedIn()
        _sessionState.value = if (isLoggedIn) {
            SocialSessionState.LoggedIn(tokenStore.getUsername().orEmpty())
        } else {
            SocialSessionState.LoggedOut
        }
        if (isLoggedIn) {
            refreshCurrentUser()
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
        refreshCurrentUser()
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
        _currentUser.value = null
    }

    fun currentUsername(): String? {
        val state = _sessionState.value as? SocialSessionState.LoggedIn ?: return null
        return state.username.takeIf { it.isNotBlank() }
    }

    fun isLoggedIn(): Boolean = _sessionState.value is SocialSessionState.LoggedIn

    fun refreshCurrentUser() {
        if (!isLoggedIn()) return
        scope.launch {
            profileRepository.get().getCurrentUserProfile().onSuccess { response ->
                val rawUrl = response.memberImage?.imageUrl ?: response.memberImageUrl
                val resolvedUrl = when {
                    rawUrl.isNullOrEmpty() -> null
                    rawUrl.startsWith("http") -> "$rawUrl?ts=${System.currentTimeMillis()}"
                    else -> "${Constants.BASE_URL}/images/$rawUrl?ts=${System.currentTimeMillis()}"
                }

                _currentUser.value = CurrentUserState(
                    memberId = response.memberId,
                    username = response.memberUsername,
                    displayName = response.memberName,
                    bio = response.memberIntroduce ?: "",
                    avatarUrl = resolvedUrl,
                    isVerified = response.verifiedEmail,
                    postsCount = response.memberPostsCount,
                    followersCount = response.memberFollowersCount,
                    followingCount = response.followingMemberFollowCount
                )

                // Sync with DataStore for Account Switcher
                accountRepository.updateAccount(response.memberUsername) { currentAccount ->
                    currentAccount.copy(
                        name = response.memberName,
                        avatarUrl = resolvedUrl,
                        isVerified = response.verifiedEmail
                    )
                }
            }
        }
    }
}
