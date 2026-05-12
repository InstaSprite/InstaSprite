package com.instasprite.app.data.repository

import com.instasprite.app.data.network.api.CommentApi
import com.instasprite.app.data.network.model.CommentUploadRequestDto
import com.instasprite.app.data.network.model.toDomain
import com.instasprite.app.data.network.safeApiCall
import com.instasprite.app.data.network.toResult
import com.instasprite.app.data.network.toResultMessage
import com.instasprite.app.domain.model.CommentData
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val commentApi: CommentApi
) {

    suspend fun createComment(postId: Long, content: String, parentId: Long = 0L): Result<CommentData> = safeApiCall {
        commentApi.createComment(
            CommentUploadRequestDto(postId = postId, parentId = parentId, content = content)
        ).toResult().map { it.comment.toDomain() }
    }

    suspend fun deleteComment(commentId: Long): Result<String> = safeApiCall {
        commentApi.deleteComment(commentId).toResultMessage("Comment deleted")
    }

    suspend fun likeComment(commentId: Long): Result<String> = safeApiCall {
        commentApi.likeComment(commentId).toResultMessage("Comment liked")
    }

    suspend fun unlikeComment(commentId: Long): Result<String> = safeApiCall {
        commentApi.unlikeComment(commentId).toResultMessage("Comment unliked")
    }

    suspend fun getCommentsPage(postId: Long, page: Int): Result<List<CommentData>> = safeApiCall {
        commentApi.getCommentsPage(postId, page).toResult().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getReplies(commentId: Long): Result<List<CommentData>> = safeApiCall {
        commentApi.getRepliesPage(commentId, 1).toResult().map { list -> list.map { it.toDomain() } }
    }
}
