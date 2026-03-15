package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.ForgotPasswordRequestDto
import com.olaz.instasprite.data.network.model.GoogleLoginRequestDto
import com.olaz.instasprite.data.network.model.JwtDto
import com.olaz.instasprite.data.network.model.LoginRequestDto
import com.olaz.instasprite.data.network.model.OtpEnableRequestDto
import com.olaz.instasprite.data.network.model.OtpEnrollmentDto
import com.olaz.instasprite.data.network.model.OtpStatusDto
import com.olaz.instasprite.data.network.model.RefreshTokenRequestDto
import com.olaz.instasprite.data.network.model.RegisterRequestDto
import com.olaz.instasprite.data.network.model.ResetPasswordRequestDto
import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ) : Response<ResultResponse<JwtDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequestDto
    ): Response<ResultResponse<JwtDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): Response<ResultResponse<JwtDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/email/resend")
    suspend fun verifyEmail(
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/2fa/enroll")
    suspend fun enrollOtp(
    ): Response<ResultResponse<OtpEnrollmentDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/2fa/enable")
    suspend fun enable2FA(
        @Body request: OtpEnableRequestDto
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/2fa/disable")
    suspend fun disable2FA(
        @Body request: OtpEnableRequestDto
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/auth/2fa/status")
    suspend fun get2FAStatus(): Response<ResultResponse<OtpStatusDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/auth/password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<ResultResponse<Any?>>
}