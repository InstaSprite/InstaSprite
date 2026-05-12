package com.instasprite.app.data.repository

import android.util.Log
import com.instasprite.app.di.RetrofitModule
import com.instasprite.app.data.network.api.NotificationApi
import com.instasprite.app.data.network.getBodyOrError
import com.instasprite.app.data.network.model.FcmTokenRequestDto
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Log.e("NotificationRepository", "Error registering FCM token", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFcmToken(fcmToken: String): Result<Unit> {
        return try {
            val response = notificationApi.deleteFcmToken(FcmTokenRequestDto(fcmToken))
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(Unit)
            } else {
                val errorMessage = body?.message ?: "Unknown error"
                val errorCode = body?.code ?: response.code().toString()
                Log.w("NotificationRepository", "Failed to delete FCM token: $errorCode - $errorMessage")
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is CancellationException) throw e
            Log.e("NotificationRepository", "Error deleting FCM token", e)
            Result.failure(e)
        }
    }

    suspend fun getNotifications(page: Int, size: Int = 20): Result<com.instasprite.app.data.network.model.SpringPageDto<com.instasprite.app.data.network.model.NotificationDto>> {
        return try {
            val response = notificationApi.getNotifications(page, size)
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

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = notificationApi.markAsRead(notificationId)
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
