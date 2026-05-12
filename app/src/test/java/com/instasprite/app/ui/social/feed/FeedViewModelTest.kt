package com.instasprite.app.ui.social.feed


import com.instasprite.app.data.repository.AccountRepository
import com.instasprite.app.data.repository.AuthRepository
import com.instasprite.app.data.repository.FollowRepository
import com.instasprite.app.data.repository.PostRepository
import com.instasprite.app.data.repository.ProfileRepository
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.session.SocialSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {



    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var postRepository: PostRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var followRepository: FollowRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var sessionManager: SocialSessionManager

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk()
        postRepository = mockk()
        profileRepository = mockk()
        followRepository = mockk()
        accountRepository = mockk()
        sessionManager = mockk(relaxed = true)

        mockkObject(PostInteractionEvent)

        viewModel = FeedViewModel(
            authRepository,
            postRepository,
            profileRepository,
            followRepository,
            accountRepository,
            sessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(PostInteractionEvent)
    }

    @Test
    fun `toggleLikePost emits optimistic update and calls repository (like)`() = runTest(testDispatcher) {
        val postId = 42L
        coEvery { postRepository.likePost(postId) } returns Result.success("Liked")
        coEvery { PostInteractionEvent.emitLikeEvent(any(), any()) } returns Unit

        viewModel.toggleLikePost(postId = postId, currentStatus = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify optimistic update was emitted with newStatus = true
        coVerify(exactly = 1) { PostInteractionEvent.emitLikeEvent(postId, true) }
        
        // Verify repository was called
        coVerify(exactly = 1) { postRepository.likePost(postId) }
    }

    @Test
    fun `toggleLikePost reverts optimistic update on repository failure (like)`() = runTest(testDispatcher) {
        val postId = 42L
        coEvery { postRepository.likePost(postId) } returns Result.failure(RuntimeException("Network error"))
        coEvery { PostInteractionEvent.emitLikeEvent(any(), any()) } returns Unit

        viewModel.toggleLikePost(postId = postId, currentStatus = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify optimistic update was emitted with newStatus = true
        coVerify(exactly = 1) { PostInteractionEvent.emitLikeEvent(postId, true) }
        
        // Verify repository was called
        coVerify(exactly = 1) { postRepository.likePost(postId) }

        // Verify it reverted to the currentStatus = false
        coVerify(exactly = 1) { PostInteractionEvent.emitLikeEvent(postId, false) }
    }

    @Test
    fun `toggleLikePost emits optimistic update and calls repository (unlike)`() = runTest(testDispatcher) {
        val postId = 42L
        coEvery { postRepository.unlikePost(postId) } returns Result.success("Unliked")
        coEvery { PostInteractionEvent.emitLikeEvent(any(), any()) } returns Unit

        viewModel.toggleLikePost(postId = postId, currentStatus = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify optimistic update was emitted with newStatus = false
        coVerify(exactly = 1) { PostInteractionEvent.emitLikeEvent(postId, false) }
        
        // Verify repository was called
        coVerify(exactly = 1) { postRepository.unlikePost(postId) }
    }
}
