package com.olaz.instasprite.data.repository

import com.olaz.instasprite.data.network.api.FollowApi
import com.olaz.instasprite.data.network.model.ResultResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class FollowRepositoryTest {

    private lateinit var followApi: FollowApi
    private lateinit var repo: FollowRepository

    @Before
    fun setUp() {
        followApi = mockk()
        repo = FollowRepository(followApi)
    }

    // ============================
    // follow()
    // ============================

    @Test
    fun `follow returns success on 200 OK`() = runTest {
        coEvery { followApi.follow("targetUser") } returns Response.success(
            ResultResponse(status = 200, code = "F001", message = "Followed", data = true)
        )

        val result = repo.follow("targetUser")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `follow returns failure on error response`() = runTest {
        coEvery { followApi.follow("targetUser") } returns Response.error(
            400, "{}".toResponseBody()
        )

        val result = repo.follow("targetUser")
        assertTrue(result.isFailure)
    }

    @Test
    fun `follow returns failure on network exception`() = runTest {
        coEvery { followApi.follow("targetUser") } throws RuntimeException("No internet")

        val result = repo.follow("targetUser")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("No internet"))
    }

    // ============================
    // unfollow()
    // ============================

    @Test
    fun `unfollow returns success on 200 OK`() = runTest {
        coEvery { followApi.unfollow("targetUser") } returns Response.success(
            ResultResponse(status = 200, code = "F002", message = "Unfollowed", data = true)
        )

        val result = repo.unfollow("targetUser")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `unfollow returns failure on exception`() = runTest {
        coEvery { followApi.unfollow("targetUser") } throws RuntimeException("Connection reset")

        val result = repo.unfollow("targetUser")
        assertTrue(result.isFailure)
    }

    // ============================
    // getFollowings()
    // ============================

    @Test
    fun `getFollowings returns failure on error`() = runTest {
        coEvery { followApi.getFollowings("user1", any(), any()) } returns Response.error(
            500, "Internal Server Error".toResponseBody()
        )

        val result = repo.getFollowings("user1")
        assertTrue(result.isFailure)
    }

    // ============================
    // getFollowers()
    // ============================

    @Test
    fun `getFollowers returns failure on exception`() = runTest {
        coEvery { followApi.getFollowers("user1", any(), any()) } throws RuntimeException("Timeout")

        val result = repo.getFollowers("user1")
        assertTrue(result.isFailure)
    }
}
