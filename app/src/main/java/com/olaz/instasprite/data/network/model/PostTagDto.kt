package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class PostTagDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("tagX")
    val tagX: Double,
    
    @SerializedName("tagY")
    val tagY: Double,
    
    @SerializedName("member")
    val member: MemberDto
)

