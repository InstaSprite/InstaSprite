package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class FcmTokenRequestDto(
    @SerializedName("token")
    val token: String
)

