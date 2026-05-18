package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class FollowingDto(
    @SerializedName("member")
    val member: FollowerMemberDto,
    
    @SerializedName("following")
    val following: Boolean = false,
    
    @SerializedName("follower")
    val follower: Boolean = false,
    
    @SerializedName("me")
    val me: Boolean = false
)

