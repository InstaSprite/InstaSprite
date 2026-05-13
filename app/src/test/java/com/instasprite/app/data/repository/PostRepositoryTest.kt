package com.instasprite.app.data.repository

import android.content.Context
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.model.ResultResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

import com.instasprite.app.data.network.S3UploadClient

class PostRepositoryTest {

    private lateinit var postApi: PostApi
    private lateinit var s3UploadClient: S3UploadClient
    private lateinit var context: Context
    private lateinit var repo: PostRepository

    @Before
    fun setUp() {
        postApi = mockk()
        s3UploadClient = mockk(relaxed = true)
        context = mockk(relaxed = true)
        repo = PostRepository(context, postApi, s3UploadClient)
    }

    // ============================
    // likePost() — UC-FEED-04
    // ============================

    @Test
    fun `likePost returns success on 200 OK`() = runTest {
        coEvery { postApi.likePost(42L) } returns Response.success(
            ResultResponse(status = 200, code = "PL01", message = "Liked", data = "Post liked successfully")
        )

        val result = repo.likePost(42L)
        assertTrue(result.isSuccess)
        assertEquals("Liked", result.getOrNull())
    }

    @Test
    fun `likePost returns failure on error response`() = runTest {
        coEvery { postApi.likePost(42L) } returns Response.error(
            400, "{}".toResponseBody()
        )

        val result = repo.likePost(42L)
        assertTrue(result.isFailure)
    }

    @Test
    fun `likePost returns failure on network exception`() = runTest {
        coEvery { postApi.likePost(42L) } throws RuntimeException("Connection refused")

        val result = repo.likePost(42L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Connection refused"))
    }

    // ============================
    // unlikePost() — UC-FEED-04
    // ============================

    @Test
    fun `unlikePost returns success on 200 OK`() = runTest {
        coEvery { postApi.unlikePost(42L) } returns Response.success(
            ResultResponse(status = 200, code = "PL02", message = "Unliked", data = "Post unliked successfully")
        )

        val result = repo.unlikePost(42L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `unlikePost returns failure on exception`() = runTest {
        coEvery { postApi.unlikePost(42L) } throws RuntimeException("Timeout")

        val result = repo.unlikePost(42L)
        assertTrue(result.isFailure)
    }

    // ============================
    // bookmarkPost() — UC-FEED-05
    // ============================

    @Test
    fun `bookmarkPost returns success on 200 OK`() = runTest {
        coEvery { postApi.bookmarkPost(42L) } returns Response.success(
            ResultResponse(status = 200, code = "BM01", message = "Bookmarked", data = "Post bookmarked successfully")
        )

        val result = repo.bookmarkPost(42L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `bookmarkPost returns failure on error`() = runTest {
        coEvery { postApi.bookmarkPost(42L) } returns Response.error(
            401, "Unauthorized".toResponseBody()
        )

        val result = repo.bookmarkPost(42L)
        assertTrue(result.isFailure)
    }

    // ============================
    // unBookmarkPost() — UC-FEED-05
    // ============================

    @Test
    fun `unBookmarkPost returns success on 200 OK`() = runTest {
        coEvery { postApi.unBookmarkPost(42L) } returns Response.success(
            ResultResponse(status = 200, code = "BM02", message = "Unbookmarked", data = "Post unbookmarked successfully")
        )

        val result = repo.unBookmarkPost(42L)
        assertTrue(result.isSuccess)
    }

    // ============================
    // deletePost()
    // ============================

    @Test
    fun `deletePost returns success on 200 OK`() = runTest {
        coEvery { postApi.deletePost(42L) } returns Response.success(
            ResultResponse(status = 200, code = "F002", message = "Post deleted", data = "deleted")
        )

        val result = repo.deletePost(42L)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `deletePost returns failure on 403 Forbidden`() = runTest {
        coEvery { postApi.deletePost(42L) } returns Response.error(
            403, "Forbidden".toResponseBody()
        )

        val result = repo.deletePost(42L)
        assertTrue(result.isFailure)
    }

    // ============================
    // getPostPage() — UC-FEED-01
    // ============================

    @Test
    fun `getPostPage returns failure on error response`() = runTest {
        coEvery { postApi.getPostPage(null) } returns Response.error(
            500, "Internal Server Error".toResponseBody()
        )

        val result = repo.getPostPage(null)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getPostPage returns failure on exception`() = runTest {
        coEvery { postApi.getPostPage(null) } throws RuntimeException("Network error")

        val result = repo.getPostPage(null)
        assertTrue(result.isFailure)
    }

    // ============================
    // getHashtagPosts() — UC-FEED-03
    // ============================

    @Test
    fun `getHashtagPosts returns failure on error response`() = runTest {
        coEvery { postApi.getHashtagPosts(any(), any(), any()) } returns Response.error(
            404, "Not Found".toResponseBody()
        )

        val result = repo.getHashtagPosts("pixelart")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getHashtagPosts returns failure on exception`() = runTest {
        coEvery { postApi.getHashtagPosts(any(), any(), any()) } throws RuntimeException("DNS failure")

        val result = repo.getHashtagPosts("pixelart")
        assertTrue(result.isFailure)
    }
}
