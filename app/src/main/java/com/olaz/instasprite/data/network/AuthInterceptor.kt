package com.olaz.instasprite.data.network

import com.google.gson.Gson
import com.olaz.instasprite.data.network.model.JwtDto
import com.olaz.instasprite.data.network.model.RefreshTokenRequestDto
import com.olaz.instasprite.data.network.model.ResultResponse
import com.olaz.instasprite.ui.social.session.SocialSessionManager
import com.olaz.instasprite.utils.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenUtils: TokenManager,
    private val sessionManager: SocialSessionManager
) : Interceptor {
    private val gson = Gson()
    private val baseUrl = Constants.BASE_URL

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authHeader = tokenUtils.getAuthorizationHeader()

        val newRequest = if (authHeader != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", authHeader)
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        // If we get a 401 Unauthorized response, try to refresh the token
        if (response.code == 401 && authHeader != null) {
            response.close()

            val refreshToken = tokenUtils.getRefreshToken()
            if (refreshToken != null) {
                try {
                    val refreshResponse = runBlocking {
                        refreshTokenDirectly(refreshToken)
                    }

                    if (refreshResponse != null) {
                        sessionManager.onTokensRefreshed(
                            accessToken = refreshResponse.accessToken,
                            refreshToken = refreshResponse.refreshToken,
                            tokenType = refreshResponse.type,
                            username = refreshResponse.username
                        )

                        // Retry the original request with new token
                        val newAuthHeader = tokenUtils.getAuthorizationHeader()
                        val retryRequest = originalRequest.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", newAuthHeader ?: authHeader)
                            .build()

                        return chain.proceed(retryRequest)
                    }
                } catch (e: Exception) {
                    sessionManager.onLogout()
                }
            } else {
                sessionManager.onLogout()
            }
        }

        return response
    }

    private suspend fun refreshTokenDirectly(refreshToken: String): JwtDto? {
        return try {
            val client = OkHttpClient()
            val requestBody = gson.toJson(RefreshTokenRequestDto(refreshToken))
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/api/v1/auth/refresh")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val resultResponse = gson.fromJson(responseBody, ResultResponse::class.java)
                    if (resultResponse.status == 200 && resultResponse.data != null) {
                        val jwtData = gson.fromJson(
                            gson.toJson(resultResponse.data),
                            JwtDto::class.java
                        )
                        return jwtData
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
