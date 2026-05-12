package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class RefreshTokenRequestDto(
    @SerializedName("refreshToken")
    val refreshToken: String
)

