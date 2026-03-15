package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.ResultResponse
import com.olaz.instasprite.data.network.model.FollowerDto
import com.olaz.instasprite.data.network.model.FollowingDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface FollowApi {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/{followMemberUsername}/follow")
    suspend fun follow(@Path("followMemberUsername") username: String): Response<ResultResponse<Boolean>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/{followMemberUsername}/follow")
    suspend fun unfollow(@Path("followMemberUsername") username: String): Response<ResultResponse<Boolean>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/{memberUsername}/following")
    suspend fun getFollowings(@Path("memberUsername") username: String): Response<ResultResponse<List<FollowingDto>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/{memberUsername}/followers")
    suspend fun getFollowers(@Path("memberUsername") username: String): Response<ResultResponse<List<FollowerDto>>>
}


