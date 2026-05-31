package com.instasprite.app.domain.session

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.instasprite.app.data.network.SessionTokenStore
import com.instasprite.app.data.network.model.UserProfileDto
import com.instasprite.app.data.repository.AccountRepository
import com.instasprite.app.data.repository.NotificationRepository
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
import kotlinx.coroutines.tasks.await
import com.instasprite.app.data.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialSessionManager @Inject constructor(
    private val tokenStore: SessionTokenStore,
    private val profileRepository: Lazy<ProfileRepository>,
    private val accountRepository: AccountRepository,
    private val notificationRepository: Lazy<NotificationRepository>,
    private val database: AppDatabase
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
        val username = tokenStore.getUsername().orEmpty()
        _sessionState.value = if (isLoggedIn) {
            SocialSessionState.LoggedIn(username)
        } else {
            SocialSessionState.LoggedOut
        }
        if (isLoggedIn) {
            scope.launch {
                try {
                    val cachedProfile = profileRepository.get().getCachedCurrentUserProfile()
                    if (cachedProfile != null) {
                        _currentUser.value = cachedProfile.toCurrentUserState()
                    }
                } catch (e: Exception) {
                    Log.e("SocialSessionManager", "Failed to cache current user at boot", e)
                }
            }
            refreshCurrentUser()
        }
    }

    suspend fun onLoginSuccess(jwt: Jwt) {
        val username = jwt.username.orEmpty()
        tokenStore.saveTokens(
            accessToken = jwt.accessToken,
            refreshToken = jwt.refreshToken,
            tokenType = jwt.type,
            username = username
        )
        _sessionState.value = SocialSessionState.LoggedIn(username)
        
        // Fetch and cache the real profile data immediately on login success
        profileRepository.get().getCurrentUserProfile().onSuccess { response ->
            val state = response.toCurrentUserState()
            _currentUser.value = state

            accountRepository.updateAccount(response.memberUsername) { currentAccount ->
                currentAccount.copy(
                    name = response.memberName,
                    avatarUrl = state.avatarUrl,
                    isVerified = response.verifiedEmail
                )
            }
        }.onFailure {
            Log.e("SocialSessionManager", "Failed to fetch profile on login", it)
        }
    }

    suspend fun onTokensRefreshed(
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

    fun logout() {
        scope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                notificationRepository.get().deleteFcmToken(token)
            } catch (e: Exception) {
                Log.w("SocialSessionManager", "Failed to delete FCM token", e)
            } finally {
                tokenStore.clearTokens()
                _sessionState.value = SocialSessionState.LoggedOut
                _currentUser.value = null

                // Clear offline cache but keep Sprite and Color palettes
                try {
                    database.userProfileDao().clear()
                    database.postDao().clearAll()
                    database.postRemoteKeysDao().clearRemoteKeys()
                    database.notificationDao().clearAll()
                    database.notificationRemoteKeysDao().clearRemoteKeys()
                } catch (e: Exception) {
                    Log.e("SocialSessionManager", "Error clearing cache", e)
                }
            }
        }
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
                val state = response.toCurrentUserState()
                _currentUser.value = state

                // Sync with DataStore for Account Switcher
                accountRepository.updateAccount(response.memberUsername) { currentAccount ->
                    currentAccount.copy(
                        name = response.memberName,
                        avatarUrl = state.avatarUrl,
                        isVerified = response.verifiedEmail
                    )
                }
            }
        }
    }
}

private fun UserProfileDto.toCurrentUserState(): CurrentUserState {
    val rawUrl = memberImage?.imageUrl ?: memberImageUrl
    val resolvedUrl = when {
        rawUrl.isNullOrEmpty() -> null
        rawUrl.startsWith("http") -> "$rawUrl?ts=${System.currentTimeMillis()}"
        else -> "${Constants.IMG_URL}/$rawUrl?ts=${System.currentTimeMillis()}"
    }
    return CurrentUserState(
        memberId = memberId,
        username = memberUsername,
        displayName = memberName,
        bio = memberIntroduce ?: "",
        avatarUrl = resolvedUrl,
        isVerified = verifiedEmail,
        postsCount = memberPostsCount,
        followersCount = memberFollowersCount,
        followingCount = followingMemberFollowCount,
        hasPassword = hasPassword
    )
}
