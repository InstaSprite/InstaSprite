package com.instasprite.app.ui.social.session

data class CurrentUserState(
    val memberId: Int,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int
)
