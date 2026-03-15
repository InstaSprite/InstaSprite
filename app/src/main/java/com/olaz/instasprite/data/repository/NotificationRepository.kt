package com.olaz.instasprite.data.repository

import android.util.Log
import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.api.NotificationApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.FcmTokenRequestDto
import kotlin.coroutines.cancellation.CancellationException

import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationApi : NotificationApi
) {

    suspend fun registerFcmToken(fcmToken: String): Result<Unit> {
        return try {
            val response = notificationApi.registerFcmToken(FcmTokenRequestDto(fcmToken))
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Log.w("NotificationRepository", "Failed to register FCM token: $errorCode - $errorMessage")
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("NotificationRepository", "Error registering FCM token", e)
            Result.failure(e)
        }
    }
}

