package com.instasprite.app.domain.session

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
