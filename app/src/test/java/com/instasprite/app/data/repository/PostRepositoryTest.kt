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
    private lateinit var database: com.instasprite.app.data.database.AppDatabase
    private lateinit var syncManager: com.instasprite.app.data.network.sync.SyncManager
    private lateinit var postDao: com.instasprite.app.data.database.PostDao
    private lateinit var repo: PostRepository

    @Before
    fun setUp() {
        postApi = mockk()
        s3UploadClient = mockk(relaxed = true)
        context = mockk(relaxed = true)
        database = mockk(relaxed = true)
        postDao = mockk(relaxed = true)
        io.mockk.every { database.postDao() } returns postDao
        syncManager = mockk(relaxed = true)
        repo = PostRepository(context, postApi, s3UploadClient, database, syncManager)
    }

    // ============================
    // likePost() — UC-FEED-04
    // ============================

    @Test
    fun `likePost updates local database and enqueues sync mutation`() = runTest {
        val post = com.instasprite.app.data.model.PostEntity(
            postId = 42L, authorId = 1L, postContent = "", postUploadDate = "", postCommentsCount = 0L, postLikesCount = 5L,
            postBookmarkFlag = false, postLikeFlag = false, commentOptionFlag = true, likeOptionFlag = true,
            isFollowing = false, followingMemberUsernameLikedPost = null, mentionsOfContent = null, hashtags = null, postImages = null, recentComments = null
        )
        coEvery { postDao.getPostById(42L) } returns post
        coEvery { postDao.updateLikeState(42L, true, 6) } returns Unit

        val result = repo.likePost(42L)
        assertTrue(result.isSuccess)
        assertEquals("Post liked offline", result.getOrNull())

        io.mockk.coVerify { postDao.updateLikeState(42L, true, 6) }
        io.mockk.coVerify { syncManager.enqueueMutation(com.instasprite.app.data.model.MutationType.LIKE_POST, "42") }
    }

    // ============================
    // unlikePost() — UC-FEED-04
    // ============================

    @Test
    fun `unlikePost updates local database and enqueues sync mutation`() = runTest {
        val post = com.instasprite.app.data.model.PostEntity(
            postId = 42L, authorId = 1L, postContent = "", postUploadDate = "", postCommentsCount = 0L, postLikesCount = 5L,
            postBookmarkFlag = false, postLikeFlag = true, commentOptionFlag = true, likeOptionFlag = true,
            isFollowing = false, followingMemberUsernameLikedPost = null, mentionsOfContent = null, hashtags = null, postImages = null, recentComments = null
        )
        coEvery { postDao.getPostById(42L) } returns post
        coEvery { postDao.updateLikeState(42L, false, 4) } returns Unit

        val result = repo.unlikePost(42L)
        assertTrue(result.isSuccess)
        assertEquals("Post unliked offline", result.getOrNull())

        io.mockk.coVerify { postDao.updateLikeState(42L, false, 4) }
        io.mockk.coVerify { syncManager.enqueueMutation(com.instasprite.app.data.model.MutationType.UNLIKE_POST, "42") }
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
