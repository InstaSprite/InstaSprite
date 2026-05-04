package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequestDto(
    @SerializedName("email")
    val email: String
)

