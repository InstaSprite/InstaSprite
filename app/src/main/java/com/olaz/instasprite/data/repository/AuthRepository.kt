package com.olaz.instasprite.data.repository

import com.olaz.instasprite.data.network.api.AuthApi
import com.olaz.instasprite.data.network.model.ForgotPasswordRequestDto
import com.olaz.instasprite.data.network.model.GoogleLoginRequestDto
import com.olaz.instasprite.data.network.model.OtpEnableRequestDto
import com.olaz.instasprite.data.network.model.OtpEnrollmentDto
import com.olaz.instasprite.data.network.model.RefreshTokenRequestDto
import com.olaz.instasprite.data.network.model.ResetPasswordRequestDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.data.network.safeApiCall
import com.olaz.instasprite.data.network.toResult
import com.olaz.instasprite.data.network.toResultMessage
import com.olaz.instasprite.data.network.toResultUnit
import com.olaz.instasprite.domain.model.Jwt
import com.olaz.instasprite.domain.model.LoginRequest
import com.olaz.instasprite.domain.model.RegisterRequest
import com.olaz.instasprite.domain.model.toDto
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> = safeApiCall {
        authApi.register(registerRequest.toDto()).toResultUnit()
    }

    suspend fun login(loginRequest: LoginRequest): Result<Jwt> = safeApiCall {
        authApi.login(loginRequest.toDto()).toResult().map { it.toDomain() }
    }

    suspend fun loginWithGoogle(googleLoginRequestDto: GoogleLoginRequestDto): Result<Jwt> = safeApiCall {
        authApi.loginWithGoogle(googleLoginRequestDto).toResult().map { it.toDomain() }
    }

    suspend fun refreshToken(refreshToken: String): Result<Jwt> = safeApiCall {
        authApi.refreshToken(RefreshTokenRequestDto(refreshToken)).toResult().map { it.toDomain() }
    }

    suspend fun verifyEmail(): Result<String> = safeApiCall {
        authApi.verifyEmail().toResultMessage("Verification email sent")
    }

    suspend fun enrollOtp(): Result<OtpEnrollmentDto> = safeApiCall {
        authApi.enrollOtp().toResult()
    }

    suspend fun enable2FA(otpCode: String): Result<Unit> = safeApiCall {
        authApi.enable2FA(OtpEnableRequestDto(otpCode)).toResultUnit()
    }

    suspend fun disable2FA(otpCode: String): Result<Unit> = safeApiCall {
        authApi.disable2FA(OtpEnableRequestDto(otpCode)).toResultUnit()
    }

    suspend fun get2FAStatus(): Result<Boolean> = safeApiCall {
        authApi.get2FAStatus().toResult().map { it.enabled }
    }

    suspend fun forgotPassword(email: String): Result<Unit> = safeApiCall {
        authApi.forgotPassword(ForgotPasswordRequestDto(email)).toResultUnit()
    }

    suspend fun resetPassword(temporaryPassword: String, newPassword: String): Result<Unit> = safeApiCall {
        authApi.resetPassword(ResetPasswordRequestDto(temporaryPassword, newPassword)).toResultUnit()
    }
}
