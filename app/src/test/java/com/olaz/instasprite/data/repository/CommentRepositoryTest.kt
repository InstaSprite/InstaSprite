package com.olaz.instasprite.data.repository

import com.olaz.instasprite.data.network.api.CommentApi
import com.olaz.instasprite.data.network.model.ResultResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CommentRepositoryTest {

    private lateinit var commentApi: CommentApi
    private lateinit var repo: CommentRepository

    @Before
    fun setUp() {
        commentApi = mockk()
        repo = CommentRepository(commentApi)
    }

    // ============================
    // deleteComment() — UC-COMM-03
    // ============================

    @Test
    fun `deleteComment returns success on 200 OK`() = runTest {
        coEvery { commentApi.deleteComment(42L) } returns Response.success(
            ResultResponse(status = 200, code = "C002", message = "Comment deleted", data = "deleted")
        )

        val result = repo.deleteComment(42L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteComment returns failure on error response`() = runTest {
        coEvery { commentApi.deleteComment(42L) } returns Response.error(
            403, "Forbidden".toResponseBody()
        )

        val result = repo.deleteComment(42L)
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteComment returns failure on network exception`() = runTest {
        coEvery { commentApi.deleteComment(42L) } throws RuntimeException("Timeout")

        val result = repo.deleteComment(42L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Timeout"))
    }

    // ============================
    // likeComment()
    // ============================

    @Test
    fun `likeComment returns success on 200 OK`() = runTest {
        coEvery { commentApi.likeComment(42L) } returns Response.success(
            ResultResponse(status = 200, code = "CL01", message = "Liked", data = "Comment liked")
        )

        val result = repo.likeComment(42L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `likeComment returns failure on exception`() = runTest {
        coEvery { commentApi.likeComment(42L) } throws RuntimeException("Network error")

        val result = repo.likeComment(42L)
        assertTrue(result.isFailure)
    }

    // ============================
    // unlikeComment()
    // ============================

    @Test
    fun `unlikeComment returns success on 200 OK`() = runTest {
        coEvery { commentApi.unlikeComment(42L) } returns Response.success(
            ResultResponse(status = 200, code = "CL02", message = "Unliked", data = "Comment unliked")
        )

        val result = repo.unlikeComment(42L)
        assertTrue(result.isSuccess)
    }

    // ============================
    // getCommentsPage()
    // ============================

    @Test
    fun `getCommentsPage returns failure on error response`() = runTest {
        coEvery { commentApi.getCommentsPage(1L, 1) } returns Response.error(
            500, "Server Error".toResponseBody()
        )

        val result = repo.getCommentsPage(1L, 1)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getCommentsPage returns failure on exception`() = runTest {
        coEvery { commentApi.getCommentsPage(1L, 1) } throws RuntimeException("Timeout")

        val result = repo.getCommentsPage(1L, 1)
        assertTrue(result.isFailure)
    }
}
