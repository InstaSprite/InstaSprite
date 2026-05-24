package com.instasprite.app.data.network.api

import com.instasprite.app.data.network.model.FcmTokenRequestDto
import com.instasprite.app.data.network.model.GroupedNotificationDto
import com.instasprite.app.data.network.model.ResultResponse
import com.instasprite.app.data.network.model.SpringPageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface NotificationApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/notifications/fcm-token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequestDto
    ): Response<ResultResponse<Any?>>

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "/api/v1/notifications/fcm-token", hasBody = true)
    suspend fun deleteFcmToken(
        @Body request: FcmTokenRequestDto
    ): Response<ResultResponse<Any?>>

    @GET("/api/v1/notifications")
    suspend fun getNotifications(
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int = 20
    ): Response<ResultResponse<com.instasprite.app.data.network.model.SpringPageDto<com.instasprite.app.data.network.model.NotificationDto>>>

    @PUT("/api/v1/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @retrofit2.http.Path("notificationId") notificationId: String
    ): Response<ResultResponse<Any?>>

    @GET("/api/v1/notifications/grouped")
    suspend fun getGroupedNotifications(
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int = 20
    ): Response<ResultResponse<SpringPageDto<GroupedNotificationDto>>>

    @PUT("/api/v1/notifications/group/read")
    suspend fun markGroupAsRead(
        @retrofit2.http.Query("type") type: String,
        @retrofit2.http.Query("relatedEntityId") relatedEntityId: String
    ): Response<ResultResponse<Any?>>

    @PUT("/api/v1/notifications/read-all")
    suspend fun markAllAsRead(): Response<ResultResponse<Any?>>
}

