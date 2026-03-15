package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface AlarmApi {

    @Headers("Content-Type: application/json")
    @GET("/api/v1/alarms")
    suspend fun getAlarms(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ResultResponse<Any>>
}
