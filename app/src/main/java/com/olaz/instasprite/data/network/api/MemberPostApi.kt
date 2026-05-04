package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface MemberPostApi {

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/{username}/posts/recent")
    suspend fun getRecent15Posts(
        @Path("username") username: String
    ): Response<ResultResponse<List<PostDto>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/posts/saved/recent")
    suspend fun getRecent15SavedPosts(): Response<ResultResponse<List<PostDto>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/posts/saved")
    suspend fun getSavedPostPage(
        @retrofit2.http.Query("page") page: Int
    ): Response<ResultResponse<List<PostDto>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/accounts/{username}/posts/tagged/recent")
    suspend fun getRecent15TaggedPosts(
        @Path("username") username: String
    ): Response<ResultResponse<List<PostDto>>>
}


