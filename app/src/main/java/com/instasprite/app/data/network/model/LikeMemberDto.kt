package com.instasprite.app.data.network.model

data class LikeMemberDto(
    val member: MemberDto,
    val isFollowing: Boolean,
    val isFollower: Boolean
)
