package com.instasprite.app.data.network.api

import com.instasprite.app.data.network.model.EditProfileRequestDto
import com.instasprite.app.data.network.model.EditProfileResponseDto
import com.instasprite.app.data.network.model.ResultResponse
import com.instasprite.app.data.network.model.UserProfileDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/profile")
    suspend fun getCurrentUserProfile(): Response<ResultResponse<UserProfileDto>>
    
    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/accounts/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<ResultResponse<UserProfileDto>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/edit")
    suspend fun getEditProfile(): Response<ResultResponse<EditProfileResponseDto>>

    @Headers("Content-Type: application/json")
    @PUT("/api/v1/accounts/edit")
    suspend fun editProfile(
        @Body request: EditProfileRequestDto
    ): Response<ResultResponse<String>>

    @Multipart
    @POST("/api/v1/accounts/image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<ResultResponse<String>>
}