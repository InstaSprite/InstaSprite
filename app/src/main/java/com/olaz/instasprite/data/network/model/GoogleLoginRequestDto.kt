package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class GoogleLoginRequestDto(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("otpCode")
    val otpCode: String? = null

)

