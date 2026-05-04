package com.olaz.instasprite.domain.model

data class LoginRequest (
    
    val identifier: String,

    val password: String,
    
    val otpCode: String? = null
)