package com.instasprite.app.ui.social.feed

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
import com.instasprite.app.R
import com.instasprite.app.data.network.ApiError
import com.instasprite.app.data.paging.FeedPagingSource
import com.instasprite.app.data.repository.AuthRepository
import com.instasprite.app.data.repository.FollowRepository
import com.instasprite.app.data.repository.PostRepository
import com.instasprite.app.domain.model.PostData
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.ui.gallery.contract.BottomBarEvent
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.feed.contract.FeedContentState
import com.instasprite.app.domain.session.SocialSessionManager
import com.instasprite.app.domain.session.SocialSessionState
import com.instasprite.app.utils.ConnectivityObserver
import com.instasprite.app.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PostFilter {
    Follow,
    Recent
}

data class FeedUiState(
    val postFilter: PostFilter = PostFilter.Recent
)

data class VerifyEmailState(
    val showVerifyDialog: Boolean = false,
    val isSending: Boolean = false,
    val message: String = "",
    val success: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val followRepository: FollowRepository,
    private val sessionManager: SocialSessionManager,
    private val connectivityObserver: ConnectivityObserver,
    private val dialogController: DialogController<FeedDialog>,
    @ApplicationContext private val context: Context
) : ViewModel(),
    DialogController<FeedDialog> by dialogController {

    private val _contentState = MutableStateFlow(FeedContentState())
    val contentState: StateFlow<FeedContentState> = _contentState.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var currentTopPostId: Long = 0L
    private var isLoggedIn: Boolean = false
    var openSearch: () -> Unit = {}

    var profileImageRefreshCounter by mutableIntStateOf(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedPosts: Flow<PagingData<PostData>> = _contentState
        .map { it.uiState.postFilter to it.currentUser?.username }
        .distinctUntilChanged()
        .flatMapLatest { (filter, _) ->
            postRepository.getPagedPosts(filter)
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

                if (!loggedInNow) {
                    _contentState.update { it.copy(uiState = it.uiState.copy(postFilter = PostFilter.Recent)) }
                    clearFeed()
                }
            }
        }
        viewModelScope.launch {
            sessionManager.currentUser.collectLatest { user ->
                _contentState.update { it.copy(currentUser = user) }
                if (user != null && !user.isVerified) {
                    openDialog(FeedDialog.VerifyEmail)
                } else if (user != null && user.isVerified) {
                    closeAllDialogs()
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
                profileImageRefreshCounter++
                sessionManager.refreshCurrentUser()
            }
        }
    }

    suspend fun startPolling() {
        while (viewModelScope.isActive) {
            delay(30_000)
            if (currentTopPostId == 0L) continue

            val filter = _contentState.value.uiState.postFilter
            if (!isLoggedIn && filter == PostFilter.Follow) continue

            if (!_contentState.value.hasNewPosts && currentTopPostId > 0) {
                try {
                    val result = when (_contentState.value.uiState.postFilter) {
                        PostFilter.Recent -> postRepository.getRecentPostsPage(null)
                        PostFilter.Follow -> postRepository.getPostPage(null)
                    }

                    result.onSuccess { page ->
                        val serverNewestId = page.content.firstOrNull()?.postId ?: 0L
                        if (serverNewestId > currentTopPostId) {
                            _contentState.update { it.copy(hasNewPosts = true, isServerMaintenance = false) }
                        }
                    }.onFailure { error ->
                        val isDeviceOnline = isOnline.value
                        if (isDeviceOnline && (error is ApiError.Network || error is ApiError.Server || error is ApiError.Unknown)) {
                            _contentState.update { it.copy(isServerMaintenance = true) }
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

    fun onBottomBarEvent(event: BottomBarEvent) {
        when(event) {
            BottomBarEvent.OpenDisplayOptions -> openDialog(FeedDialog.PostFilter)
            BottomBarEvent.ToggleSearchBar -> openSearch()
        }
    }

    fun setPostFilter(filter: PostFilter) {
        _contentState.update {
            it.copy(uiState = it.uiState.copy(postFilter = filter))
        }
    }

    fun clearError() {
        _contentState.update {
            it.copy(
                verifyEmailState = it.verifyEmailState.copy(message = "")
            )
        }
    }

    fun retryConnection() {
        _contentState.update { it.copy(isServerMaintenance = false) }
        sessionManager.refreshCurrentUser()
    }



    fun toggleLikePost(postId: Long, currentStatus: Boolean) {
        if (!isLoggedIn) {
            _contentState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val newStatus = !currentStatus
        viewModelScope.launch(Dispatchers.IO) {
            PostInteractionEvent.emitLikeEvent(postId, newStatus)
            val result =
                if (newStatus) postRepository.likePost(postId) else postRepository.unlikePost(postId)
            result.onFailure { PostInteractionEvent.emitLikeEvent(postId, currentStatus) }
        }
    }

    fun toggleBookmarkPost(postId: Long, currentStatus: Boolean) {
        if (!isLoggedIn) {
            _contentState.update { it.copy(showLoginRequiredError = true) }
            return
        }
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
        if (!isLoggedIn) {
            _contentState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        if (_contentState.value.currentUser?.username.equals(
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
                    closeTopDialog()
                },
                onFailure = { error ->
                    _contentState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = false,
                                message = error.toUserMessage(context)
                            )
                        )
                    }
                }
            )
        }
    }

    fun deletePost(postId: Long) {
        if (!isLoggedIn) {
            _contentState.update { it.copy(showLoginRequiredError = true) }
            return
        }
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
                currentUser = null,
                verifyEmailState = VerifyEmailState(),
                refreshPending = false,
                hasNewPosts = false,
                deletedPostIds = emptySet()
            )
        }
    }

    fun consumeLoginRequiredError() {
        _contentState.update { it.copy(showLoginRequiredError = false) }
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
            sessionManager.refreshCurrentUser()
            closeAllDialogs()
        }
    }
}
