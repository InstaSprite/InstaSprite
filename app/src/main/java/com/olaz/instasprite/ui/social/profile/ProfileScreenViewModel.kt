package com.olaz.instasprite.ui.social.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.network.model.EditProfileRequestDto
import com.olaz.instasprite.data.network.model.FollowerDto
import com.olaz.instasprite.data.repository.AccountRepository
import com.olaz.instasprite.data.repository.FollowRepository
import com.olaz.instasprite.data.repository.ProfileRepository
import com.olaz.instasprite.ui.social.PostInteractionEvent
import com.olaz.instasprite.ui.social.profile.contract.FollowerUser
import com.olaz.instasprite.ui.social.profile.contract.ProfileContentState
import com.olaz.instasprite.ui.social.profile.contract.UserProfileState
import com.olaz.instasprite.ui.social.session.SocialSessionManager
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.utils.Constants
import com.olaz.instasprite.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import coil3.imageLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
    private val accountRepository: AccountRepository,
    private val sessionManager: SocialSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _contentState = MutableStateFlow(ProfileContentState())
    val contentState: StateFlow<ProfileContentState> = _contentState.asStateFlow()

    private val isLoggedIn: Boolean
        get() = sessionManager.sessionState.value is SocialSessionState.LoggedIn

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            PostInteractionEvent.profileRefreshEvent.collectLatest {
                val currentProfile = _contentState.value.userProfile
                if (currentProfile.username.isNotBlank()) {
                    if (currentProfile.isOwnProfile) {
                        loadCurrentUserProfile()
                    } else {
                        loadUserProfile(currentProfile.username)
                    }
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.followStateChangeEvents.collectLatest { (username, isFollowing) ->
                val currentProfile = _contentState.value.userProfile
                if (currentProfile.username.equals(username, ignoreCase = true) &&
                    currentProfile.isFollowing != isFollowing
                ) {
                    _contentState.update { state ->
                        state.copy(
                            userProfile = state.userProfile.copy(
                                isFollowing = isFollowing,
                                followersCount = if (isFollowing) {
                                    state.userProfile.followersCount + 1
                                } else {
                                    (state.userProfile.followersCount - 1).coerceAtLeast(0)
                                }
                            )
                        )
                    }
                }

                _contentState.update { state ->
                    state.copy(
                        followers = state.followers.map { user ->
                            if (user.username.equals(username, ignoreCase = true)) {
                                user.copy(isFollowing = isFollowing)
                            } else {
                                user
                            }
                        },
                        following = state.following.map { user ->
                            if (user.username.equals(username, ignoreCase = true)) {
                                user.copy(isFollowing = isFollowing)
                            } else {
                                user
                            }
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.postCreatedEvent.collectLatest {
                if (_contentState.value.userProfile.isOwnProfile) {
                    loadCurrentUserProfile()
                }
            }
        }
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentUsername = sessionManager.currentUsername()
            val result = if (currentUsername != null) {
                profileRepository.getUserProfile(currentUsername)
            } else {
                profileRepository.getCurrentUserProfile()
            }
            
            result.fold(
                onSuccess = { data ->
                    val userProfile = mapUserProfileResponseToUserProfile(data)
                    _contentState.update {
                        it.copy(isLoading = false, errorMessage = null, userProfile = userProfile)
                    }
                    refreshPosts(userProfile.username)
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(isLoading = true, errorMessage = null) }
            profileRepository.getUserProfile(userId).fold(
                onSuccess = { data ->
                    val userProfile = mapUserProfileResponseToUserProfile(data)
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            userProfile = userProfile,
                            userPosts = emptyList(),
                            sharedPosts = emptyList(),
                            selectedTabIndex = if (userProfile.isOwnProfile) it.selectedTabIndex else 0
                        )
                    }
                    refreshPosts(userProfile.username)
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    private suspend fun refreshPosts(username: String) {
        profileRepository.getRecentPostsByUsername(username).onSuccess { posts ->
            _contentState.update { it.copy(userPosts = posts) }
        }
    }

    fun toggleEditProfileDialog() {
        _contentState.update { it.copy(showEditProfileDialog = !it.showEditProfileDialog) }
    }

    fun toggleEditAvatarDialog() {
        _contentState.update { it.copy(showEditAvatarDialog = !it.showEditAvatarDialog) }
    }

    fun selectTab(tabIndex: Int) {
        _contentState.update { it.copy(selectedTabIndex = tabIndex) }
        viewModelScope.launch(Dispatchers.IO) {
            if (tabIndex == 1) {
                if (_contentState.value.userProfile.isOwnProfile) {
                    profileRepository.getRecentSavedPosts().fold(
                        onSuccess = { posts ->
                            _contentState.update { it.copy(sharedPosts = posts) }
                        },
                        onFailure = {
                            _contentState.update { it.copy(sharedPosts = emptyList()) }
                        }
                    )
                } else {
                    _contentState.update { it.copy(sharedPosts = emptyList()) }
                }
            }
        }
    }

    fun updateUserProfile(displayName: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(isLoading = true, errorMessage = null) }

            profileRepository.getEditProfile().fold(
                onSuccess = { editData ->
                    val currentProfile = _contentState.value.userProfile
                    val request = EditProfileRequestDto(
                        memberUsername = currentProfile.username,
                        memberName = displayName,
                        memberIntroduce = bio,
                        memberEmail = editData.memberEmail
                    )

                    profileRepository.editProfile(request).fold(
                        onSuccess = {
                            sessionManager.currentUsername()?.let { currentUsername ->
                                viewModelScope.launch(Dispatchers.IO) {
                                    accountRepository.updateAccount(currentUsername) { acc ->
                                        acc.copy(name = displayName)
                                    }
                                }
                            }
                            _contentState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    userProfile = it.userProfile.copy(
                                        displayName = displayName,
                                        bio = bio
                                    )
                                )
                            }
                        },
                        onFailure = { error ->
                            _contentState.update {
                                it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    fun toggleFollow() {
        if (!isLoggedIn) {
            _contentState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val currentProfile = _contentState.value.userProfile
        if (currentProfile.isOwnProfile) return

        val targetUsername = currentProfile.username
        val currentIsFollowing = currentProfile.isFollowing
        val newFollowState = !currentIsFollowing

        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitFollowEvent(targetUsername, newFollowState)

            val result = if (newFollowState) {
                followRepository.follow(targetUsername)
            } else {
                followRepository.unfollow(targetUsername)
            }

            result.onFailure { error ->
                PostInteractionEvent.emitFollowEvent(targetUsername, currentIsFollowing)
                _contentState.update {
                    it.copy(errorMessage = error.toUserMessage(context))
                }
            }
        }
    }

    fun consumeLoginRequiredError() {
        _contentState.update { it.copy(showLoginRequiredError = false) }
    }

    fun clearError() {
        _contentState.update { it.copy(errorMessage = null) }
    }

    fun showFollowersDialog() {
        _contentState.update { it.copy(showFollowersDialog = true) }
        loadFollowers()
    }

    fun hideFollowersDialog() {
        _contentState.update { it.copy(showFollowersDialog = false) }
    }

    fun showFollowingDialog() {
        _contentState.update { it.copy(showFollowingDialog = true) }
        loadFollowing()
    }

    fun hideFollowingDialog() {
        _contentState.update { it.copy(showFollowingDialog = false) }
    }

    private fun loadFollowers() {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(followersLoading = true) }
            val username = _contentState.value.userProfile.username
            followRepository.getFollowers(username).fold(
                onSuccess = { followers ->
                    _contentState.update {
                        it.copy(
                            followers = followers.content.map(::mapFollowerDto),
                            followersLoading = false
                        )
                    }
                },
                onFailure = {
                    _contentState.update { it.copy(followersLoading = false) }
                }
            )
        }
    }

    private fun loadFollowing() {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(followingLoading = true) }
            val username = _contentState.value.userProfile.username
            followRepository.getFollowings(username).fold(
                onSuccess = { following ->
                    _contentState.update {
                        it.copy(
                            following = following.content.map(::mapFollowerDto),
                            followingLoading = false
                        )
                    }
                },
                onFailure = {
                    _contentState.update { it.copy(followingLoading = false) }
                }
            )
        }
    }

    fun followUser(userId: String) {
        val user = _contentState.value.followers.find { it.id == userId }
            ?: _contentState.value.following.find { it.id == userId }
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitFollowEvent(user.username, true)
            followRepository.follow(user.username).onFailure {
                PostInteractionEvent.emitFollowEvent(user.username, false)
            }
        }
    }

    fun unfollowUser(userId: String) {
        val user = _contentState.value.followers.find { it.id == userId }
            ?: _contentState.value.following.find { it.id == userId }
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitFollowEvent(user.username, false)
            followRepository.unfollow(user.username).onFailure {
                PostInteractionEvent.emitFollowEvent(user.username, true)
            }
        }
    }

    fun loadProfileImage(userId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update {
                it.copy(
                    profileImageUiState = it.profileImageUiState.copy(isLoading = true, error = null)
                )
            }

            val currentUsername = sessionManager.currentUsername()
            val result = if (userId.isNullOrBlank() && currentUsername != null) {
                profileRepository.getUserProfile(currentUsername)
            } else if (!userId.isNullOrBlank()) {
                profileRepository.getUserProfile(userId)
            } else {
                profileRepository.getCurrentUserProfile()
            }

            result.fold(
                onSuccess = { data ->
                    val imageUrl = data.memberImage?.imageUrl ?: data.memberImageUrl
                    val finalUrl = if (!imageUrl.isNullOrEmpty()) {
                        if (imageUrl.startsWith("http")) {
                            "$imageUrl?ts=${System.currentTimeMillis()}"
                        } else {
                            "${Constants.BASE_URL}/images/$imageUrl?ts=${System.currentTimeMillis()}"
                        }
                    } else {
                        null
                    }
                    
                    if (userId.isNullOrBlank() || userId == sessionManager.currentUsername()) {
                        sessionManager.currentUsername()?.let { currentUsername ->
                            viewModelScope.launch(Dispatchers.IO) {
                                accountRepository.updateAccount(currentUsername) { acc ->
                                    acc.copy(avatarUrl = finalUrl)
                                }
                            }
                        }
                    }

                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false, imageUrl = finalUrl, error = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false, imageUrl = null, error = error.toUserMessage(context)
                            )
                        )
                    }
                }
            )
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update {
                it.copy(
                    profileImageUiState = it.profileImageUiState.copy(isLoading = true, error = null)
                )
            }

            profileRepository.uploadProfileImage(imageUri, context).fold(
                onSuccess = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()

                    if (_contentState.value.userProfile.isOwnProfile) {
                        loadProfileImage()
                        loadCurrentUserProfile()
                    }
                    _contentState.update { it.copy(showEditAvatarDialog = false) }
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false, error = error.toUserMessage(context)
                            )
                        )
                    }
                }
            )
        }
    }

    private fun mapFollowerDto(dto: FollowerDto): FollowerUser {
        return FollowerUser(
            id = dto.member.id.toString(),
            username = dto.member.username,
            displayName = dto.member.name,
            profileImageUrl = dto.member.image?.imageUrl?.let { imageUrl ->
                "${Constants.BASE_URL}/images/$imageUrl"
            },
            isFollowing = dto.following
        )
    }

    private fun mapUserProfileResponseToUserProfile(response: com.olaz.instasprite.data.network.model.UserProfileDto): UserProfileState {
        return UserProfileState(
            id = response.memberId.toString(),
            username = response.memberUsername,
            displayName = response.memberName,
            bio = response.memberIntroduce ?: "",
            email = "",
            profileImageUrl = response.memberImage?.imageUrl ?: response.memberImageUrl,
            postsCount = response.memberPostsCount,
            followersCount = response.memberFollowersCount,
            followingCount = response.memberFollowingsCount,
            isFollowing = response.following,
            isOwnProfile = response.me,
            isBlocked = response.blocked,
            isBlocking = response.blocking
        )
    }
}
