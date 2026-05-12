package com.instasprite.app.domain.model

data class LoginRequest (
    
    val identifier: String,

    val password: String,
    
    val otpCode: String? = null
)