package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.FcmTokenRequestDto
import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/notifications/fcm-token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequestDto
    ): Response<ResultResponse<Any?>>
}

