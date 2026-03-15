package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class MemberDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("image")
    val image: MemberImageDto? = null
)

