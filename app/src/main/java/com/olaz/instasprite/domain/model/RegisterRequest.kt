package com.olaz.instasprite.domain.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(

    val name: String? = null,

    val username: String? = null,

    val email: String? = null,

    val password: String
)
