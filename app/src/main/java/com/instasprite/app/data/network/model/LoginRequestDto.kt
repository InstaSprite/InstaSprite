package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDto (
    @SerializedName("identifier")
    val identifier: String,

    @SerializedName("password")
    val password: String,
    
    @SerializedName("otpCode")
    val otpCode: String? = null
)