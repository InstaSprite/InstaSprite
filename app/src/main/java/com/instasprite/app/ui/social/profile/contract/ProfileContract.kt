package com.instasprite.app.ui.social.profile.contract

import com.instasprite.app.domain.model.PostData

data class ProfileContentState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTabIndex: Int = 0,
    val showFollowersDialog: Boolean = false,
    val showFollowingDialog: Boolean = false,
    val userProfile: UserProfileState = UserProfileState(),
    val userPosts: List<PostData> = emptyList(),
    val sharedPosts: List<PostData> = emptyList(),
    val followers: List<FollowerUser> = emptyList(),
    val following: List<FollowerUser> = emptyList(),
    val followersLoading: Boolean = false,
    val followingLoading: Boolean = false,
    val showLoginRequiredError: Boolean = false,
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
    val onFollowClick: () -> Unit = {},
    val onFollowersClick: () -> Unit = {},
    val onFollowingClick: () -> Unit = {},
    val onTabSelected: (Int) -> Unit = {},
    val onFollowUser: (userId: String) -> Unit = {},
    val onUnfollowUser: (userId: String) -> Unit = {},
    val onPostClick: (postId: Long) -> Unit = {},
    val onMenuClick: () -> Unit = {},
    val onClearError: () -> Unit = {},
    val onDismissFollowers: () -> Unit = {},
    val onDismissFollowing: () -> Unit = {},
    val onConsumeLoginRequiredError: () -> Unit = {},
    val onNavigateToEditProfile: () -> Unit = {},
)
