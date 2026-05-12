package com.instasprite.app.domain.model

data class Jwt(
    val type: String,
    val accessToken: String,
    val refreshToken: String,
    val name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val isFirstTime: Boolean? = null
)