package com.instasprite.app.domain.model

import com.instasprite.app.data.network.model.LoginRequestDto
import com.instasprite.app.data.network.model.RegisterRequestDto

fun LoginRequest.toDto(): LoginRequestDto {
    return LoginRequestDto(
        identifier = this.identifier,
        password = this.password,
        otpCode = this.otpCode
    )
}

fun RegisterRequest.toDto(): RegisterRequestDto {
    return RegisterRequestDto(
        username = this.username,
        name = this.name,
        email = this.email,
        password = this.password
    )
}