package com.olaz.instasprite.ui.social.feed

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.olaz.instasprite.R
import com.olaz.instasprite.data.paging.FeedPagingSource
import com.olaz.instasprite.data.repository.AccountRepository
import com.olaz.instasprite.data.repository.AuthRepository
import com.olaz.instasprite.data.repository.FollowRepository
import com.olaz.instasprite.data.repository.PostRepository
import com.olaz.instasprite.data.repository.ProfileRepository
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.social.PostInteractionEvent
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.session.SocialSessionManager
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PostFilter {
    Follow,
    Recent
}

data class FeedUiState(
    val showPostFilterDialog: Boolean = false,
    val postFilter: PostFilter = PostFilter.Recent
)

data class ProfileImageState(
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null
)

data class VerifyEmailState(
    val showVerifyDialog: Boolean = false,
    val isSending: Boolean = false,
    val message: String = "",
    val success: Boolean = false
)

data class ProfileState(
    val isLoading: Boolean = false,
    val memberName: String = "",
    val memberUsername: String = "",
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
    private val accountRepository: AccountRepository,
    private val sessionManager: SocialSessionManager
) : ViewModel() {

    private val _contentState = MutableStateFlow(FeedContentState())
    val contentState: StateFlow<FeedContentState> = _contentState.asStateFlow()

    private var currentTopPostId: Long = 0L
    private var isLoggedIn: Boolean = false

    var profileImageRefreshCounter by mutableIntStateOf(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedPosts: Flow<PagingData<PostData>> = _contentState
        .map { it.uiState.postFilter to it.profileState.memberUsername }
        .distinctUntilChanged()
        .flatMapLatest { (filter, _) ->
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    FeedPagingSource(
                        fetchLogic = { cursor ->
                            when (filter) {
                                PostFilter.Follow -> postRepository.getPostPage(cursor)
                                PostFilter.Recent -> postRepository.getRecentPostsPage(cursor)
                            }
                        }
                    )
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    init {
        _contentState.update { it.copy(pagedPosts = pagedPosts) }

        observeSession()
        observeEvents()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.sessionState.collectLatest { sessionState ->
                val loggedInNow = sessionState is SocialSessionState.LoggedIn
                isLoggedIn = loggedInNow

                if (loggedInNow) {
                    getCurrentProfile()
                    loadProfileImage()
                } else {
                    clearFeed()
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            PostInteractionEvent.postLikeEvent.collectLatest { (postId, isLiked) ->
                _contentState.update {
                    it.copy(localLikeState = it.localLikeState + (postId to isLiked))
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.postBookmarkEvent.collectLatest { (postId, isBookmarked) ->
                _contentState.update {
                    it.copy(localBookmarkState = it.localBookmarkState + (postId to isBookmarked))
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.postCommentEvent.collectLatest { (postId, delta) ->
                _contentState.update { state ->
                    val currentDelta = state.localCommentState[postId] ?: 0L
                    state.copy(
                        localCommentState = state.localCommentState + (postId to (currentDelta + delta))
                    )
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.followStateChangeEvents.collectLatest { (username, isFollowing) ->
                _contentState.update {
                    it.copy(localFollowState = it.localFollowState + (username to isFollowing))
                }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.postCreatedEvent.collectLatest {
                _contentState.update { it.copy(refreshPending = true) }
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.emailVerificationEvent.collectLatest {
                handleEmailVerification()
            }
        }

        viewModelScope.launch {
            PostInteractionEvent.profileRefreshEvent.collectLatest {
                forceReloadProfileImage()
                getCurrentProfile()
            }
        }
    }

    suspend fun startPolling() {
        while (viewModelScope.isActive) {
            delay(30_000)
            if (!isLoggedIn) continue
            if (currentTopPostId == 0L) continue

            if (!_contentState.value.hasNewPosts && currentTopPostId > 0) {
                try {
                    val result = when (_contentState.value.uiState.postFilter) {
                        PostFilter.Recent -> postRepository.getRecentPostsPage(null)
                        PostFilter.Follow -> postRepository.getPostPage(null)
                    }

                    result.onSuccess { page ->
                        val serverNewestId = page.content.firstOrNull()?.postId ?: 0L
                        if (serverNewestId > currentTopPostId) {
                            _contentState.update { it.copy(hasNewPosts = true) }
                        }
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.d("FeedViewModel", it) }
                }
            }
        }
    }

    fun onRefreshed() {
        _contentState.update { it.copy(hasNewPosts = false) }
    }

    fun togglePostFilterDialog() {
        _contentState.update {
            it.copy(uiState = it.uiState.copy(showPostFilterDialog = !it.uiState.showPostFilterDialog))
        }
    }

    fun setPostFilter(filter: PostFilter) {
        _contentState.update {
            it.copy(uiState = it.uiState.copy(postFilter = filter))
        }
    }

    fun getCurrentProfile() {
        if (!isLoggedIn) return
        if (_contentState.value.profileState.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update {
                it.copy(profileState = it.profileState.copy(isLoading = true, error = null))
            }
            try {
                val response = profileRepository.getCurrentUserProfile()
                if (response.status == 200 && response.data != null) {
                    _contentState.update {
                        it.copy(
                            profileState = it.profileState.copy(
                                isLoading = false,
                                memberName = response.data.memberName,
                                memberUsername = response.data.memberUsername,
                                error = null
                            )
                        )
                    }

                    val avatarUrl = response.data.memberImage?.imageUrl?.let {
                        "${Constants.BASE_URL}/images/$it"
                    }.orEmpty()

                    accountRepository.updateAccount(username = response.data.memberUsername) { currentAccount ->
                        currentAccount.copy(
                            name = response.data.memberName,
                            avatarUrl = avatarUrl,
                            isVerified = response.data.verifiedEmail
                        )
                    }

                    if (!response.data.verifiedEmail) {
                        _contentState.update {
                            it.copy(
                                verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = true)
                            )
                        }
                    }
                } else {
                    _contentState.update {
                        it.copy(
                            profileState = it.profileState.copy(
                                isLoading = false,
                                error = response.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        profileState = it.profileState.copy(
                            isLoading = false,
                            error = e.message
                        )
                    )
                }
            }
        }
    }

    fun loadProfileImage() {
        if (!isLoggedIn) return
        if (_contentState.value.profileImageState.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            _contentState.update {
                it.copy(
                    profileImageState = it.profileImageState.copy(
                        isLoading = true,
                        error = null
                    )
                )
            }
            try {
                val response = profileRepository.getCurrentUserProfile()
                if (response.status == 200 && response.data != null) {
                    val imageUrl = response.data.memberImage?.imageUrl
                    val finalUrl = if (!imageUrl.isNullOrEmpty()) {
                        "${Constants.BASE_URL}/images/$imageUrl?ts=${System.currentTimeMillis()}"
                    } else {
                        null
                    }
                    _contentState.update {
                        it.copy(
                            profileImageState = it.profileImageState.copy(
                                isLoading = false,
                                imageUrl = finalUrl,
                                error = null
                            )
                        )
                    }
                } else {
                    _contentState.update {
                        it.copy(
                            profileImageState = it.profileImageState.copy(
                                isLoading = false,
                                error = response.message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _contentState.update {
                    it.copy(
                        profileImageState = it.profileImageState.copy(
                            isLoading = false,
                            error = e.message
                        )
                    )
                }
            }
        }
    }

    fun forceRefreshProfileImage() {
        profileImageRefreshCounter++
    }

    fun forceReloadProfileImage() {
        _contentState.update { it.copy(profileImageState = ProfileImageState()) }
        forceRefreshProfileImage()
        loadProfileImage()
    }

    fun toggleLikePost(postId: Long, currentStatus: Boolean) {
        val newStatus = !currentStatus
        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitLikeEvent(postId, newStatus)
            val result =
                if (newStatus) postRepository.likePost(postId) else postRepository.unlikePost(postId)
            result.onFailure { PostInteractionEvent.emitLikeEvent(postId, currentStatus) }
        }
    }

    fun toggleBookmarkPost(postId: Long, currentStatus: Boolean) {
        val newStatus = !currentStatus
        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitBookmarkEvent(postId, newStatus)
            val result = if (newStatus) {
                postRepository.bookmarkPost(postId)
            } else {
                postRepository.unBookmarkPost(postId)
            }
            result.onFailure { PostInteractionEvent.emitBookmarkEvent(postId, currentStatus) }
        }
    }

    fun toggleFollow(username: String, currentStatus: Boolean) {
        if (_contentState.value.profileState.memberUsername.equals(
                username,
                ignoreCase = true
            )
        ) return
        val newStatus = !currentStatus
        val normalizedUsername = username.trim()

        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitFollowEvent(normalizedUsername, newStatus)
            val result = if (newStatus) {
                followRepository.follow(normalizedUsername)
            } else {
                followRepository.unfollow(normalizedUsername)
            }
            result.onFailure {
                PostInteractionEvent.emitFollowEvent(
                    normalizedUsername,
                    currentStatus
                )
            }
        }
    }

    fun dismissVerifyDialog() {
        _contentState.update {
            it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = false))
        }
    }

    fun verifyEmail(context: Context) {
        _contentState.update {
            it.copy(verifyEmailState = it.verifyEmailState.copy(isSending = true))
        }
        viewModelScope.launch {
            authRepository.verifyEmail().fold(
                onSuccess = {
                    _contentState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = true
                            )
                        )
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.email_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismissVerifyDialog()
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = false,
                                message = error.message.orEmpty()
                            )
                        )
                    }
                }
            )
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            _contentState.update { it.copy(deletedPostIds = it.deletedPostIds + postId) }
            val result = postRepository.deletePost(postId)
            if (result.isFailure) {
                _contentState.update { it.copy(deletedPostIds = it.deletedPostIds - postId) }
            }
        }
    }

    fun clearFeed() {
        currentTopPostId = 0L
        _contentState.update {
            it.copy(
                localLikeState = emptyMap(),
                localBookmarkState = emptyMap(),
                localCommentState = emptyMap(),
                localFollowState = emptyMap(),
                profileState = ProfileState(),
                profileImageState = ProfileImageState(),
                verifyEmailState = VerifyEmailState(),
                refreshPending = false,
                hasNewPosts = false,
                deletedPostIds = emptySet()
            )
        }
    }

    fun updateTopPostId(newId: Long) {
        if (newId > currentTopPostId) {
            currentTopPostId = newId
            _contentState.update { it.copy(hasNewPosts = false) }
        }
    }

    fun consumeRefreshPending() {
        _contentState.update { it.copy(refreshPending = false) }
    }

    private fun handleEmailVerification() {
        viewModelScope.launch(Dispatchers.IO) {
            getCurrentProfile()
            _contentState.update {
                it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = false))
            }
        }
    }
}
