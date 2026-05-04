package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class FollowerMemberDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("image")
    val image: FollowerImageDto? = null
)

