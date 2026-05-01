package com.olaz.instasprite.data.repository

import android.util.Log
import com.olaz.instasprite.data.network.api.AuthApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.ForgotPasswordRequestDto
import com.olaz.instasprite.data.network.model.GoogleLoginRequestDto
import com.olaz.instasprite.data.network.model.OtpEnableRequestDto
import com.olaz.instasprite.data.network.model.OtpEnrollmentDto
import com.olaz.instasprite.data.network.model.RefreshTokenRequestDto
import com.olaz.instasprite.data.network.model.ResetPasswordRequestDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.domain.model.Jwt
import com.olaz.instasprite.domain.model.LoginRequest
import com.olaz.instasprite.domain.model.RegisterRequest
import com.olaz.instasprite.domain.model.toDto
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        return try {
            val response = authApi.register(registerRequest.toDto())
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<Jwt> {
        return try {
            val response = authApi.login(loginRequest.toDto())
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data.toDomain())
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Log.d("AuthRepository", "Login failed: $errorCode - $errorMessage")
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(googleLoginRequestDto: GoogleLoginRequestDto): Result<Jwt> {
        return try {
            val response = authApi.loginWithGoogle(googleLoginRequestDto)
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data.toDomain())
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<Jwt> {
        return try {
            val response = authApi.refreshToken(RefreshTokenRequestDto(refreshToken))
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data.toDomain())
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun verifyEmail(): Result<String> {
        return try {
            val response = authApi.verifyEmail()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.message)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun enrollOtp(): Result<OtpEnrollmentDto> {
        return try {
            val response = authApi.enrollOtp()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun enable2FA(otpCode: String): Result<Unit> {
        return try {
            val response = authApi.enable2FA(OtpEnableRequestDto(otpCode))
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun disable2FA(otpCode: String): Result<Unit> {
        return try {
            val response = authApi.disable2FA(OtpEnableRequestDto(otpCode))
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun get2FAStatus(): Result<Boolean> {
        return try {
            val response = authApi.get2FAStatus()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data.enabled)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequestDto(email))
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun resetPassword(temporaryPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response =
                authApi.resetPassword(ResetPasswordRequestDto(temporaryPassword, newPassword))
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}


