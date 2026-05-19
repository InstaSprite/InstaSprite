package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class UserProfileDto(
    @SerializedName("memberId")
    val memberId: Int,
    
    @SerializedName("memberUsername")
    val memberUsername: String,
    
    @SerializedName("memberName")
    val memberName: String,
    
    @SerializedName("memberImage")
    val memberImage: MemberImageDto? = null,
    
    @SerializedName("memberImageUrl")
    val memberImageUrl: String? = null,
    
    @SerializedName("memberIntroduce")
    val memberIntroduce: String? = null,
    
    @SerializedName("memberPostsCount")
    val memberPostsCount: Int,
    
    @SerializedName("memberFollowingsCount")
    val memberFollowingsCount: Int,
    
    @SerializedName("memberFollowersCount")
    val memberFollowersCount: Int,
    
    @SerializedName("followingMemberFollow")
    val followingMemberFollow: List<FollowingMemberFollowItemDto> = emptyList(),
    
    @SerializedName("followingMemberFollowCount")
    val followingMemberFollowCount: Int = 0,
    
    @SerializedName("blocking")
    val blocking: Boolean = false,
    
    @SerializedName("following")
    val following: Boolean = false,
    
    @SerializedName("me")
    val me: Boolean = false,
    
    @SerializedName("follower")
    val follower: Boolean = false,
    
    @SerializedName("blocked")
    val blocked: Boolean = false,

    @SerializedName("verifiedEmail")
    val verifiedEmail: Boolean = false,

    @SerializedName("hasPassword")
    val hasPassword: Boolean = true
)

