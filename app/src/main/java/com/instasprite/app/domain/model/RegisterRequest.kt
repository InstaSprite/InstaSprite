package com.instasprite.app.domain.model

data class RegisterRequest(

    val name: String? = null,

    val username: String? = null,

    val email: String? = null,

    val password: String
)
