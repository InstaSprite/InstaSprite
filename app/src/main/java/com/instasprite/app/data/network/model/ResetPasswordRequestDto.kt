package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequestDto(
    @SerializedName("temporaryPassword")
    val temporaryPassword: String,
    
    @SerializedName("newPassword")
    val newPassword: String
)

