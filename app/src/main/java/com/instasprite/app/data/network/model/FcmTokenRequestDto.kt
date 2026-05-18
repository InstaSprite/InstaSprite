package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class FcmTokenRequestDto(
    @SerializedName("token")
    val token: String
)

