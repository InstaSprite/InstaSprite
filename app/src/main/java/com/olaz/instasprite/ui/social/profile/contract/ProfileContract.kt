package com.olaz.instasprite.ui.social.profile.contract

import android.net.Uri
import com.olaz.instasprite.domain.model.PostData

data class ProfileContentState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showEditProfileDialog: Boolean = false,
    val showEditAvatarDialog: Boolean = false,
    val selectedTabIndex: Int = 0,
    val showFollowersDialog: Boolean = false,
    val showFollowingDialog: Boolean = false,
    val userProfile: UserProfileState = UserProfileState(),
    val userPosts: List<PostData> = emptyList(),
    val sharedPosts: List<PostData> = emptyList(),
    val profileImageUiState: ProfileImageUiState = ProfileImageUiState(),
    val followers: List<FollowerUser> = emptyList(),
    val following: List<FollowerUser> = emptyList(),
    val followersLoading: Boolean = false,
    val followingLoading: Boolean = false,
)

data class UserProfileState(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val isOwnProfile: Boolean = true,
    val isBlocked: Boolean = false,
    val isBlocking: Boolean = false,
    val joinDate: Long = System.currentTimeMillis()
)

data class ProfileImageUiState(
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null
)

data class FollowerUser(
    val id: String,
    val username: String,
    val displayName: String,
    val profileImageUrl: String?,
    val isFollowing: Boolean = false
)

enum class ProfileTab(val title: String) {
    POSTS("Post"),
    SAVED("Saved")
}

data class ProfileScreenEvent(
    val onBackClick: () -> Unit = {},
    val onEditProfileClick: () -> Unit = {},
    val onEditAvatarClick: () -> Unit = {},
    val onFollowClick: () -> Unit = {},
    val onFollowersClick: () -> Unit = {},
    val onFollowingClick: () -> Unit = {},
    val onTabSelected: (Int) -> Unit = {},
    val onUpdateProfile: (name: String, bio: String) -> Unit = { _, _ -> },
    val onFollowUser: (userId: String) -> Unit = {},
    val onUnfollowUser: (userId: String) -> Unit = {},
    val onUploadAvatar: (Uri) -> Unit = {},
    val onPostClick: (postId: Long) -> Unit = {},
    val onMenuClick: () -> Unit = {},
    val onClearError: () -> Unit = {},
    val onDismissEditProfile: () -> Unit = {},
    val onDismissAvatarDialog: () -> Unit = {},
    val onDismissFollowers: () -> Unit = {},
    val onDismissFollowing: () -> Unit = {},
)
