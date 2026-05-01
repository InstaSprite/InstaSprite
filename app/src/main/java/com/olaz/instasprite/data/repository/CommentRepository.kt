package com.olaz.instasprite.data.repository

import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.api.CommentApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.CommentUploadRequestDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.domain.model.CommentData

import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val commentApi: CommentApi
) {

    suspend fun createComment(postId: Long, content: String, parentId: Long = 0L): Result<CommentData> {
        return try {
            val response = commentApi.createComment(
                CommentUploadRequestDto(
                    postId = postId,
                    parentId = parentId,
                    content = content
                )
            )
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val comment = body.data.comment.toDomain()
                Result.success(comment)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: Long): Result<String> {
        return try {
            val response = commentApi.deleteComment(commentId)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Comment deleted")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: Long): Result<String> {
        return try {
            val response = commentApi.likeComment(commentId)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Comment liked")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun unlikeComment(commentId: Long): Result<String> {
        return try {
            val response = commentApi.unlikeComment(commentId)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Comment unliked")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getCommentsPage(postId: Long, page: Int): Result<List<CommentData>> {
        return try {
            val response = commentApi.getCommentsPage(postId, page)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val comments = body.data.map { it.toDomain() }
                Result.success(comments)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getReplies(commentId: Long): Result<List<CommentData>> {
        return try {
            val response = commentApi.getRepliesPage(commentId, 1)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val comments = body.data.map { it.toDomain() }
                Result.success(comments)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}


