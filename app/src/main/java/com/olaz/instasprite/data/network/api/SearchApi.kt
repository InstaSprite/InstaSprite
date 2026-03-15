package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search")
    suspend fun searchText(
        @Query("text") text: String
    ): Response<ResultResponse<List<Any>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search/recommend")
    suspend fun getRecommendMembers(): Response<ResultResponse<List<Any>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search/auto/member")
    suspend fun getMemberAutoComplete(
        @Query("text") text: String
    ): Response<ResultResponse<List<Any>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search/auto/hashtag")
    suspend fun getHashtagAutoComplete(
        @Query("text") text: String
    ): Response<ResultResponse<List<Any>>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/search/mark")
    suspend fun markSearchedEntity(
        @Query("entityName") entityName: String,
        @Query("entityType") entityType: String
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search/recent/top")
    suspend fun getTop15RecentSearch(): Response<ResultResponse<Any>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/search/recent")
    suspend fun getRecentSearch(
        @Query("page") page: Int
    ): Response<ResultResponse<Any>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/search/recent")
    suspend fun deleteRecentSearch(
        @Query("entityName") entityName: String,
        @Query("entityType") entityType: String
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/search/recent/all")
    suspend fun deleteAllRecentSearch(): Response<ResultResponse<String>>
}
