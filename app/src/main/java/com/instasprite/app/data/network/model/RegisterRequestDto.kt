package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("password")
    val password: String
)

