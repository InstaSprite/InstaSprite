package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class FollowingMemberFollowItemDto(
    @SerializedName("memberUsername")
    val memberUsername: String
)

