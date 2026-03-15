package com.olaz.instasprite.ui.social.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.R
import com.olaz.instasprite.data.network.model.EditProfileRequestDto
import com.olaz.instasprite.data.network.model.FollowerDto
import com.olaz.instasprite.data.network.model.FollowingDto
import com.olaz.instasprite.data.network.model.UserProfileDto
import com.olaz.instasprite.data.repository.FollowRepository
import com.olaz.instasprite.data.repository.ProfileRepository
import com.olaz.instasprite.ui.social.PostInteractionEvent
import com.olaz.instasprite.ui.social.profile.contract.FollowerUser
import com.olaz.instasprite.ui.social.profile.contract.ProfileContentState
import com.olaz.instasprite.ui.social.profile.contract.UserProfileState
import com.olaz.instasprite.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _contentState = MutableStateFlow(ProfileContentState())
    val contentState: StateFlow<ProfileContentState> = _contentState.asStateFlow()

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

            try {
                val response = profileRepository.getCurrentUserProfile()
                if (response.status == 200 && response.data != null) {
                    val userProfile = mapUserProfileResponseToUserProfile(response.data)
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            userProfile = userProfile
                        )
                    }
                    refreshPosts(userProfile.username)
                    Log.d(
                        "ProfileScreenViewModel",
                        "Profile loaded successfully: ${userProfile.username}"
                    )
                } else {
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = response.message
                                ?: context.getString(R.string.failed_to_load_profile)
                        )
                    }
                    Log.e("ProfileScreenViewModel", "Failed to load profile: ${response.message}")
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: context.getString(R.string.unknown_error_occurred)
                    )
                }
                Log.e("ProfileScreenViewModel", "Error loading profile", e)
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = profileRepository.getUserProfile(userId)
                if (response.status == 200 && response.data != null) {
                    val userProfile = mapUserProfileResponseToUserProfile(response.data)
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
                } else {
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = response.message
                                ?: context.getString(R.string.failed_to_load_profile)
                        )
                    }
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: context.getString(R.string.unknown_error_occurred)
                    )
                }
            }
        }
    }

    private suspend fun refreshPosts(username: String) {
        try {
            profileRepository.getRecentPostsByUsername(username).fold(
                onSuccess = { posts ->
                    Log.d("ProfileScreenViewModel", "Loaded ${posts.size} posts for $username")
                    _contentState.update { it.copy(userPosts = posts) }
                },
                onFailure = { error ->
                    Log.e("ProfileScreenViewModel", "Failed to load posts for $username", error)
                }
            )
        } catch (e: Exception) {
            Log.e("ProfileScreenViewModel", "Exception loading posts", e)
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
            try {
                if (tabIndex == 1) {
                    if (_contentState.value.userProfile.isOwnProfile) {
                        profileRepository.getRecentSavedPosts().fold(
                            onSuccess = { posts ->
                                Log.d("ProfileScreenViewModel", "Loaded ${posts.size} saved posts")
                                _contentState.update { it.copy(sharedPosts = posts) }
                            },
                            onFailure = { error ->
                                Log.e("ProfileScreenViewModel", "Failed to load saved posts", error)
                                _contentState.update { it.copy(sharedPosts = emptyList()) }
                            }
                        )
                    } else {
                        _contentState.update { it.copy(sharedPosts = emptyList()) }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateUserProfile(displayName: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val editProfileResponse = profileRepository.getEditProfile()
                if (editProfileResponse.status != 200 || editProfileResponse.data == null) {
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.failed_to_get_current_user_data)
                        )
                    }
                    return@launch
                }

                val currentProfile = _contentState.value.userProfile
                val request = EditProfileRequestDto(
                    memberUsername = currentProfile.username,
                    memberName = displayName,
                    memberIntroduce = bio,
                    memberEmail = editProfileResponse.data.memberEmail
                )

                val response = profileRepository.editProfile(request)
                if (response.status == 200) {
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
                    Log.d("ProfileScreenViewModel", "Profile updated successfully")
                } else {
                    _contentState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = response.message
                                ?: context.getString(R.string.failed_to_update_profile)
                        )
                    }
                    Log.e("ProfileScreenViewModel", "Failed to update profile: ${response.message}")
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: context.getString(R.string.unknown_error_occurred)
                    )
                }
                Log.e("ProfileScreenViewModel", "Error updating profile", e)
            }
        }
    }

    fun toggleFollow() {
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

            result.onFailure { exception ->
                PostInteractionEvent.emitFollowEvent(targetUsername, currentIsFollowing)
                _contentState.update {
                    it.copy(
                        errorMessage = exception.message
                            ?: context.getString(R.string.failed_to_update_follow_status)
                    )
                }
            }
        }
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
            try {
                val username = _contentState.value.userProfile.username
                followRepository.getFollowers(username).fold(
                    onSuccess = { followers ->
                        _contentState.update {
                            it.copy(
                                followers = followers.map(::mapFollowerDto),
                                followersLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("ProfileScreenViewModel", "Failed to load followers", error)
                        _contentState.update { it.copy(followersLoading = false) }
                    }
                )
            } catch (e: Exception) {
                Log.e("ProfileScreenViewModel", "Error in loadFollowers", e)
                _contentState.update { it.copy(followersLoading = false) }
            }
        }
    }

    private fun loadFollowing() {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update { it.copy(followingLoading = true) }
            try {
                val username = _contentState.value.userProfile.username
                followRepository.getFollowings(username).fold(
                    onSuccess = { following ->
                        _contentState.update {
                            it.copy(
                                following = following.map(::mapFollowingDto),
                                followingLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("ProfileScreenViewModel", "Failed to load following", error)
                        _contentState.update { it.copy(followingLoading = false) }
                    }
                )
            } catch (e: Exception) {
                Log.e("ProfileScreenViewModel", "Error in loadFollowing", e)
                _contentState.update { it.copy(followingLoading = false) }
            }
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
                    profileImageUiState = it.profileImageUiState.copy(
                        isLoading = true,
                        error = null
                    )
                )
            }
            try {
                val response = if (userId.isNullOrBlank()) {
                    profileRepository.getCurrentUserProfile()
                } else {
                    profileRepository.getUserProfile(userId)
                }
                if (response.status == 200 && response.data != null) {
                    val imageUrl = response.data.memberImage?.imageUrl
                    val finalUrl = if (!imageUrl.isNullOrEmpty()) {
                        "${Constants.BASE_URL}/images/$imageUrl?ts=${System.currentTimeMillis()}"
                    } else {
                        null
                    }
                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false,
                                imageUrl = finalUrl,
                                error = null
                            )
                        )
                    }
                } else {
                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false,
                                imageUrl = null,
                                error = context.getString(R.string.failed_to_load_profile)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        profileImageUiState = it.profileImageUiState.copy(
                            isLoading = false,
                            imageUrl = null,
                            error = e.message ?: context.getString(R.string.unknown_error)
                        )
                    )
                }
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update {
                it.copy(
                    profileImageUiState = it.profileImageUiState.copy(
                        isLoading = true,
                        error = null
                    )
                )
            }
            try {
                val response = profileRepository.uploadProfileImage(imageUri, context)
                if (response.status == 200) {
                    if (_contentState.value.userProfile.isOwnProfile) {
                        loadProfileImage()
                    }
                    _contentState.update { it.copy(showEditAvatarDialog = false) }
                } else {
                    _contentState.update {
                        it.copy(
                            profileImageUiState = it.profileImageUiState.copy(
                                isLoading = false,
                                error = response.message
                                    ?: context.getString(R.string.failed_to_upload_image)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        profileImageUiState = it.profileImageUiState.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to upload image"
                        )
                    )
                }
            }
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

    private fun mapFollowingDto(dto: FollowingDto): FollowerUser {
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

    private fun mapUserProfileResponseToUserProfile(response: UserProfileDto): UserProfileState {
        return UserProfileState(
            id = response.memberId.toString(),
            username = response.memberUsername,
            displayName = response.memberName,
            bio = response.memberIntroduce ?: "",
            email = "",
            profileImageUrl = response.memberImage?.imageUrl,
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
