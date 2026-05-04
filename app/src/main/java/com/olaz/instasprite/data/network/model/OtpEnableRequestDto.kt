package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class OtpEnableRequestDto(
    @SerializedName("otpCode")
    val otpCode: String
)

