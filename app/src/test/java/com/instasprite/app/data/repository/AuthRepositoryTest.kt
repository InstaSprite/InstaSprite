package com.instasprite.app.data.repository

import android.util.Log
import com.instasprite.app.data.network.api.AuthApi
import com.instasprite.app.data.network.model.JwtDto
import com.instasprite.app.data.network.model.ResultResponse
import com.instasprite.app.domain.model.LoginRequest
import com.instasprite.app.domain.model.RegisterRequest
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

class AuthRepositoryTest {

    private lateinit var authApi: AuthApi
    private lateinit var repo: AuthRepository

    @Before
    fun setUp() {
        // Mock android.util.Log since it's unavailable in JVM tests
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        authApi = mockk()
        repo = AuthRepository(authApi)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ============================
    // register() — UC-AUTH-02
    // ============================

    @Test
    fun `register returns success on 200 OK`() = runTest {
        coEvery { authApi.register(any()) } returns Response.success(
            ResultResponse(status = 200, code = "M006", message = "Registration successful", data = null)
        )

        val result = repo.register(
            RegisterRequest(username = "newuser", name = "New User", email = "new@example.com", password = "pass123")
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `register returns failure on duplicate email`() = runTest {
        coEvery { authApi.register(any()) } returns Response.error(
            409, """{"status":409,"code":"M002","message":"Email already registered","data":null}""".toResponseBody()
        )

        val result = repo.register(
            RegisterRequest(username = "newuser", name = "New User", email = "existing@example.com", password = "pass123")
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `register returns failure on network exception`() = runTest {
        coEvery { authApi.register(any()) } throws RuntimeException("No internet")

        val result = repo.register(
            RegisterRequest(username = "newuser", name = "New User", email = "new@example.com", password = "pass123")
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("No internet"))
    }

    // ============================
    // login() — UC-AUTH-01
    // ============================

    @Test
    fun `login returns JWT on 200 OK`() = runTest {
        val jwtDto = JwtDto(
            type = "Bearer",
            accessToken = "access.token.here",
            refreshToken = "refresh.token.here",
            username = "testuser",
            name = "Test User",
            email = "test@example.com"
        )
        coEvery { authApi.login(any()) } returns Response.success(
            ResultResponse(status = 200, code = "M001", message = "Login successful", data = jwtDto)
        )

        val result = repo.login(LoginRequest(identifier = "testuser", password = "pass123"))
        assertTrue(result.isSuccess)
        val jwt = result.getOrNull()!!
        assertEquals("access.token.here", jwt.accessToken)
        assertEquals("refresh.token.here", jwt.refreshToken)
    }

    @Test
    fun `login returns failure on invalid credentials`() = runTest {
        coEvery { authApi.login(any()) } returns Response.error(
            401, """{"status":401,"code":"M003","message":"Invalid credentials","data":null}""".toResponseBody()
        )

        val result = repo.login(LoginRequest(identifier = "wronguser", password = "wrongpass"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `login returns failure on network exception`() = runTest {
        coEvery { authApi.login(any()) } throws RuntimeException("Connection refused")

        val result = repo.login(LoginRequest(identifier = "testuser", password = "pass123"))
        assertTrue(result.isFailure)
    }
}
