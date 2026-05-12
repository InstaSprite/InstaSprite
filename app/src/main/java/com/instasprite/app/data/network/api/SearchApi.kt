package com.instasprite.app.data.network.api

import com.instasprite.app.data.network.model.PostDto
import com.instasprite.app.data.network.model.ResultResponse
import com.instasprite.app.data.network.model.SearchResponseDto
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    @Headers("Content-Type: application/json")
    @GET("/api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ResultResponse<SearchResponseDto>>

    @Headers("Guest-Aware: true")
    @GET("/api/v1/search/trending")
    suspend fun getTrendingPosts(): Response<ResultResponse<List<PostDto>>>
}
