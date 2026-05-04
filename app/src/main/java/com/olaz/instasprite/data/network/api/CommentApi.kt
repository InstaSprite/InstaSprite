package com.olaz.instasprite.data.network.api

import com.olaz.instasprite.data.network.model.CommentDto
import com.olaz.instasprite.data.network.model.CommentUploadRequestDto
import com.olaz.instasprite.data.network.model.CreateCommentResponseDto
import com.olaz.instasprite.data.network.model.ResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface CommentApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/comments")
    suspend fun createComment(
        @Body body: CommentUploadRequestDto
    ): Response<ResultResponse<CreateCommentResponseDto>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long
    ): Response<ResultResponse<String>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/posts/{postId}")
    suspend fun getCommentsPage(
        @Path("postId") postId: Long,
        @Query("page") page: Int
    ): Response<ResultResponse<List<CommentDto>>>


    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/comments/{commentId}")
    suspend fun getRepliesPage(
        @Path("commentId") commentId: Long,
        @Query("page") page: Int
    ): Response<ResultResponse<List<CommentDto>>>

    @Headers("Guest-Aware: true", "Content-Type: application/json")
    @GET("/api/v1/comments/{commentId}")
    suspend fun getReplies(
        @Path("commentId") commentId: Long
    ): Response<ResultResponse<List<CommentDto>>>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/comments/like")
    suspend fun likeComment(
        @Query("commentId") commentId: Long
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @DELETE("/api/v1/comments/like")
    suspend fun unlikeComment(
        @Query("commentId") commentId: Long
    ): Response<ResultResponse<String>>

    @Headers("Content-Type: application/json")
    @GET("/api/v1/comments/{commentId}/likes")
    suspend fun getCommentLikes(
        @Path("commentId") commentId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ResultResponse<List<Any>>> // TODO: Create MemberDto or similar for likes
}


