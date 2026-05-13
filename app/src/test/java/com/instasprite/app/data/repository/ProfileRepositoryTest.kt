package com.instasprite.app.data.repository

import android.util.Log
import com.instasprite.app.data.network.api.MemberPostApi
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.api.ProfileApi
import com.instasprite.app.data.network.model.ResultResponse
import com.instasprite.app.data.network.model.UserProfileDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryTest {

    private lateinit var profileApi: ProfileApi
    private lateinit var memberPostApi: MemberPostApi
    private lateinit var postApi: PostApi
    private lateinit var repo: ProfileRepository

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0

        profileApi = mockk()
        memberPostApi = mockk()
        postApi = mockk()
        repo = ProfileRepository(profileApi, memberPostApi, postApi)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ============================
    // getUserProfile()
    // ============================

    @Test
    fun `getUserProfile returns UserProfileDto on 200 OK`() = runTest {
        val userProfileDto = UserProfileDto(
            memberId = 1,
            memberUsername = "testuser",
            memberName = "Test User",
            memberIntroduce = "Hello",
            memberPostsCount = 0,
            memberFollowersCount = 100,
            memberFollowingsCount = 50,
            following = false
        )
        coEvery { profileApi.getUserProfile("testuser") } returns Response.success(
            ResultResponse(status = 200, code = "P001", message = "Success", data = userProfileDto)
        )

        val result = repo.getUserProfile("testuser")
        assertTrue(result.isSuccess)
        assertEquals("testuser", result.getOrNull()?.memberUsername)
    }

    @Test
    fun `getUserProfile returns fallback on error response`() = runTest {
        coEvery { profileApi.getUserProfile("testuser") } returns Response.error(
            404, "Not Found".toResponseBody()
        )

        val result = repo.getUserProfile("testuser")
        assertTrue(result.isFailure)
    }

    // ============================
    // getCurrentUserProfile()
    // ============================

    @Test
    fun `getCurrentUserProfile returns UserProfileDto on 200 OK`() = runTest {
        val userProfileDto = UserProfileDto(
            memberId = 2,
            memberUsername = "me",
            memberName = "Me",
            memberIntroduce = "My bio",
            memberPostsCount = 0,
            memberFollowersCount = 10,
            memberFollowingsCount = 5,
            following = false
        )
        coEvery { profileApi.getCurrentUserProfile() } returns Response.success(
            ResultResponse(status = 200, code = "P002", message = "Success", data = userProfileDto)
        )

        val result = repo.getCurrentUserProfile()
        assertTrue(result.isSuccess)
        assertEquals("me", result.getOrNull()?.memberUsername)
    }

    // ============================
    // getRecentPostsByUsername()
    // ============================

    @Test
    fun `getRecentPostsByUsername returns failure on error response`() = runTest {
        coEvery { memberPostApi.getRecent15Posts("user1") } returns Response.error(
            500, "Internal Server Error".toResponseBody()
        )

        val result = repo.getRecentPostsByUsername("user1")
        assertTrue(result.isFailure)
    }
}
