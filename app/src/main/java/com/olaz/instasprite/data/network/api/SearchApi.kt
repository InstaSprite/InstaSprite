package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    @Headers("Content-Type: application/json")
    @GET("/api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ResultResponse<com.olaz.instasprite.data.network.model.SearchResponseDto>>
}
