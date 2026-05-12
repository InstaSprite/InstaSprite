package com.instasprite.app.data.repository

import com.instasprite.app.data.model.AlarmData
import com.instasprite.app.data.network.api.AlarmApi
import com.instasprite.app.data.network.getBodyOrError
import com.instasprite.app.di.RetrofitModule
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmApi: AlarmApi
) {

    suspend fun getAlarms(page: Int, size: Int): Result<List<AlarmData>> {
        return try {
            val response = alarmApi.getAlarms(page, size)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get alarms"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}
