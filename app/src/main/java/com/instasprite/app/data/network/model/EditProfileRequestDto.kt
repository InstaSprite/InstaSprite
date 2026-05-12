package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class EditProfileRequestDto(
    @SerializedName("memberUsername")
    val memberUsername: String,
    
    @SerializedName("memberName")
    val memberName: String,
    
    @SerializedName("memberIntroduce")
    val memberIntroduce: String? = null,
    
    @SerializedName("memberEmail")
    val memberEmail: String
)

