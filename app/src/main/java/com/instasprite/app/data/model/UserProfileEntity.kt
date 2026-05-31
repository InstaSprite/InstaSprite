package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instasprite.app.data.network.model.FollowingMemberFollowItemDto
import com.instasprite.app.data.network.model.MemberImageDto

@Entity(tableName = "current_user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val memberId: Int,
    val memberUsername: String,
    val memberName: String,
    val memberImage: MemberImageDto?,
    val memberImageUrl: String?,
    val memberIntroduce: String?,
    val memberPostsCount: Int,
    val memberFollowingsCount: Int,
    val memberFollowersCount: Int,
    val followingMemberFollow: List<FollowingMemberFollowItemDto>,
    val followingMemberFollowCount: Int,
    val blocking: Boolean,
    val following: Boolean,
    val me: Boolean,
    val follower: Boolean,
    val blocked: Boolean,
    val verifiedEmail: Boolean,
    val hasPassword: Boolean
)
