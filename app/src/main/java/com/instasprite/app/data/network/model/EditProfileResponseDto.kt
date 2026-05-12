package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class EditProfileResponseDto(
    @SerializedName("memberUsername")
    val memberUsername: String,
    
    @SerializedName("memberImageUrl")
    val memberImageUrl: String,
    
    @SerializedName("memberName")
    val memberName: String,
    
    @SerializedName("memberIntroduce")
    val memberIntroduce: String? = null,
    
    @SerializedName("memberEmail")
    val memberEmail: String
)

