package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.PageDto
import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.ResultResponse
import com.olaz.instasprite.data.network.model.SpringPageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApi {

    @Multipart
    @POST("/api/v1/posts")
    suspend fun uploadPost(
        @Part("content") content: RequestBody,
        @Part postImages: List<MultipartBody.Part>,
        @Part altTexts: List<MultipartBody.Part>,
        @Part("commentFlag") commentFlag: RequestBody
    ): Response<ResultResponse<Any>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/posts")
    suspend fun getPostPage(
        @Query("cursor") lastPostId: Long?
    ): Response<ResultResponse<PageDto>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/posts/recent")
    suspend fun getRecent10Posts(): Response<ResultResponse<List<PostDto>>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/posts/recent/page")
    suspend fun getRecentPostsPage(
        @Query("cursor") lastPostId: Long?
    ): Response<ResultResponse<PageDto>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/posts")
    suspend fun deletePost(
        @Query("postId") postId: Long
    ): Response<ResultResponse<String>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Long
    ): Response<ResultResponse<PostDto>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/posts/like")
    suspend fun likePost(
        @Query("postId") postId: Long
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/posts/like")
    suspend fun unlikePost(
        @Query("postId") postId: Long
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/posts/save")
    suspend fun bookmarkPost(
        @Query("postId") postId: Long
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/posts/save")
    suspend fun unBookmarkPost(
        @Query("postId") postId: Long
    ): Response<ResultResponse<String>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/posts/hashtags")
    suspend fun getHashtagPosts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("hashtag") hashtag: String
    ): Response<ResultResponse<SpringPageDto<PostDto>>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/posts/most-liked")
    suspend fun getMostLikedPost(): Response<ResultResponse<PostDto>>
}

