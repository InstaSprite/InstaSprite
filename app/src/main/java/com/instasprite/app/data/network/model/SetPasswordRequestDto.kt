package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class SetPasswordRequestDto(
    @SerializedName("password")
    val password: String
)
