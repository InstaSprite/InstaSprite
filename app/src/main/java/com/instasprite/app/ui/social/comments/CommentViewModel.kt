package com.instasprite.app.ui.social.comments

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.R
import com.instasprite.app.data.network.ApiError
import com.instasprite.app.data.network.api.FollowApi
import com.instasprite.app.data.network.api.ProfileApi
import com.instasprite.app.data.repository.AuthRepository
import com.instasprite.app.data.repository.CommentRepository
import com.instasprite.app.data.repository.PostRepository
import com.instasprite.app.domain.model.CommentData
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.comments.contract.Comment
import com.instasprite.app.ui.social.comments.contract.CommentState
import com.instasprite.app.ui.social.comments.contract.PostAuthor
import com.instasprite.app.domain.session.SocialSessionManager
import com.instasprite.app.domain.session.SocialSessionState
import com.instasprite.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject
import com.instasprite.app.utils.toUserMessage

@HiltViewModel
class CommentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val followApi: FollowApi,
    private val profileApi: ProfileApi,
    private val sessionManager: SocialSessionManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences(
            "comment_likes",
            Context.MODE_PRIVATE
        )
    }

    private fun getLikedComments(): Set<String> =
        prefs.getStringSet("liked_ids", emptySet()) ?: emptySet()

    private fun setLiked(commentId: String, liked: Boolean) {
        val current = getLikedComments().toMutableSet()
        if (liked) current.add(commentId) else current.remove(commentId)
        prefs.edit().putStringSet("liked_ids", current).apply()
    }

    private val _uiState = MutableStateFlow(CommentState())
    val uiState: StateFlow<CommentState> = _uiState.asStateFlow()

    fun loadPost(postId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = postRepository.getPost(postId)
            result.fold(
                onSuccess = { p ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            backendPost = p,
                            likesCount = p.postLikesCount.toInt(),
                            isLiked = p.postLikeFlag,
                            isBookmarked = p.postBookmarkFlag,
                            postAuthor = PostAuthor(
                                id = p.member.memberId.toString(),
                                username = p.member.memberUsername,
                                displayName = p.member.memberName,
                                profileImageRes = R.drawable.ic_launcher,
                                isFollowing = p.isFollowing
                            ),
                            isOwnPost = sessionManager.currentUsername()
                                ?.equals(p.member.memberUsername, ignoreCase = true) == true
                        )
                    }
                    loadFullComments(postId)
                },
                onFailure = { e ->
                    val errorMsg = e.toUserMessage(context)
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
            )
        }
    }

    private fun mapCommentDataToUi(
        commentData: CommentData,
        currentUsername: String?,
        parentId: Long? = null
    ): Comment {
        val persistedLiked = getLikedComments()
        return Comment(
            id = commentData.id.toString(),
            userId = commentData.member.memberId.toString(),
            username = commentData.member.memberUsername,
            displayName = commentData.member.memberName,
            profileImageRes = R.drawable.ic_launcher,
            profileImageUrl = commentData.member.memberImage?.imageUrl,
            content = commentData.content,
            createdAt = commentData.uploadDate.atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli(),
            likesCount = commentData.commentLikesCount,
            isLiked = commentData.commentLikeFlag || persistedLiked.contains(commentData.id.toString()),
            isOwnComment = currentUsername != null && currentUsername.equals(
                commentData.member.memberUsername,
                ignoreCase = true
            ),
            parentId = parentId?.toString()
        )
    }

    private fun loadFullComments(postId: Long) {
        viewModelScope.launch {
            try {
                val currentUsername = sessionManager.currentUsername()
                val uiComments = mutableListOf<Comment>()
                val seenIds = mutableSetOf<String>()

                val p = _uiState.value.backendPost
                val recent = p?.recentComments ?: emptyList()
                val persistedLiked = getLikedComments()
                recent.forEach { c ->
                    val item = Comment(
                        id = c.id.toString(),
                        userId = c.member.memberId.toString(),
                        username = c.member.memberUsername,
                        displayName = c.member.memberName,
                        profileImageRes = R.drawable.ic_launcher,
                        profileImageUrl = c.member.memberImage?.imageUrl,
                        content = c.content,
                        createdAt = c.uploadDate.atZone(ZoneId.systemDefault()).toInstant()
                            .toEpochMilli(),
                        likesCount = c.commentLikesCount,
                        isLiked = c.commentLikeFlag || persistedLiked.contains(c.id.toString()),
                        isOwnComment = currentUsername != null && currentUsername.equals(
                            c.member.memberUsername,
                            ignoreCase = true
                        )
                    )
                    uiComments.add(item)
                    seenIds.add(item.id)
                }

                for (c in recent) {
                    if (c.repliesCount > 0) {
                        val repliesRes = commentRepository.getReplies(c.id)
                        repliesRes.onSuccess { replies ->
                            replies.forEach { r ->
                                val replyItem =
                                    mapCommentDataToUi(r, currentUsername, parentId = c.id)
                                if (!seenIds.contains(replyItem.id)) {
                                    val parentIndex =
                                        uiComments.indexOfFirst { it.id == c.id.toString() }
                                    if (parentIndex >= 0) {
                                        uiComments.add(parentIndex + 1, replyItem)
                                    } else {
                                        uiComments.add(replyItem)
                                    }
                                    seenIds.add(replyItem.id)
                                }
                            }
                        }
                    }
                }

                val topLevelResult = commentRepository.getCommentsPage(postId, 1)
                topLevelResult.onSuccess { list ->
                    for (c in list) {
                        val parent = mapCommentDataToUi(c, currentUsername)
                        if (!seenIds.contains(parent.id)) {
                            uiComments.add(parent)
                            seenIds.add(parent.id)
                        }
                    }
                    _uiState.update { it.copy(comments = uiComments) }
                }.onFailure {
                    _uiState.update { it.copy(comments = uiComments) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.toUserMessage(context)) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    private val isLoggedIn: Boolean
        get() = sessionManager.sessionState.value is SocialSessionState.LoggedIn

    fun toggleLike() {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val postId = _uiState.value.backendPost?.postId ?: return
        val currentLiked = _uiState.value.isLiked
        val newLiked = !currentLiked
        val previousLikes = _uiState.value.likesCount
        val newLikes = if (newLiked) previousLikes + 1 else maxOf(0, previousLikes - 1)

        _uiState.update { it.copy(isLiked = newLiked, likesCount = newLikes) }

        viewModelScope.launch {
            val result =
                if (newLiked) postRepository.likePost(postId) else postRepository.unlikePost(postId)
            PostInteractionEvent.emitLikeEvent(postId, newLiked)
            result.onFailure {
                _uiState.update { it.copy(isLiked = currentLiked, likesCount = previousLikes) }
            }
        }
    }

    fun toggleBookmark() {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val postId = _uiState.value.backendPost?.postId ?: return
        val currentBookmarked = _uiState.value.isBookmarked
        val newBookmarked = !currentBookmarked

        _uiState.update { it.copy(isBookmarked = newBookmarked) }

        viewModelScope.launch {
            val result =
                if (newBookmarked) postRepository.bookmarkPost(postId) else postRepository.unBookmarkPost(
                    postId
                )
            PostInteractionEvent.emitBookmarkEvent(postId, newBookmarked)
            result.onFailure {
                _uiState.update { it.copy(isBookmarked = currentBookmarked) }
            }
        }
    }

    private fun toFullImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null
        return if (imageUrl.startsWith("http")) imageUrl else "${Constants.IMG_URL}/$imageUrl"
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val response = profileApi.getCurrentUserProfile()
                if (response.body()?.status == 200) {
                    val imageUrl = toFullImageUrl(response.body()?.data?.memberImage?.imageUrl)
                    _uiState.update { it.copy(currentUserImageUrl = imageUrl) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.toUserMessage(context)) }
            }
        }
    }

    fun toggleFollow() {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val author = _uiState.value.postAuthor ?: return
        val newFollowing = !author.isFollowing

        _uiState.update { it.copy(postAuthor = author.copy(isFollowing = newFollowing)) }

        viewModelScope.launch {
            try {
                val response = if (newFollowing) {
                    followApi.follow(author.username)
                } else {
                    followApi.unfollow(author.username)
                }
                if (response.body()?.status == 200) {
                    PostInteractionEvent.emitFollowEvent(
                        username = author.username,
                        isFollowing = newFollowing
                    )
                } else {
                    _uiState.update { it.copy(postAuthor = author) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(postAuthor = author) }
            }
        }
    }

    fun startReply(parentCommentId: Long) {
        _uiState.update { it.copy(replyParentId = parentCommentId) }
    }

    fun clearReplyTarget() {
        _uiState.update { it.copy(replyParentId = null) }
    }

    fun toggleCommentLike(commentId: String) {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val previous = _uiState.value.comments
        val target = previous.find { it.id == commentId } ?: return
        val newLiked = !target.isLiked
        val updated = previous.map { c ->
            if (c.id == commentId) {
                c.copy(
                    isLiked = newLiked,
                    likesCount = if (newLiked) c.likesCount + 1 else maxOf(0, c.likesCount - 1)
                )
            } else c
        }
        _uiState.update { it.copy(comments = updated) }

        viewModelScope.launch {
            val idLong = commentId.toLongOrNull() ?: return@launch
            val result =
                if (newLiked) commentRepository.likeComment(idLong) else commentRepository.unlikeComment(
                    idLong
                )
            result.onFailure {
                _uiState.update { it.copy(comments = previous) }
            }.onSuccess {
                setLiked(commentId, newLiked)
            }
        }
    }

    fun deleteComment(commentId: String) {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        val postId = _uiState.value.backendPost?.postId ?: return
        val prev = _uiState.value.comments
        _uiState.update { it.copy(comments = prev.filter { it.id != commentId && it.parentId != commentId }) }

        viewModelScope.launch {
            val idLong = commentId.toLongOrNull() ?: return@launch
            val result = commentRepository.deleteComment(idLong)
            result.onFailure {
                _uiState.update { it.copy(comments = prev) }
            }.onSuccess {
                PostInteractionEvent.emitCommentEvent(postId, -1)
            }
        }
    }

    fun addComment(content: String) {
        if (!isLoggedIn) {
            _uiState.update { it.copy(showLoginRequiredError = true) }
            return
        }
        if (content.isBlank()) return
        val postId = _uiState.value.backendPost?.postId ?: return
        val parentId = _uiState.value.replyParentId ?: 0L
        val tempId = "temp_${System.currentTimeMillis()}"
        val username = sessionManager.currentUsername() ?: context.getString(R.string.you)
        val optimistic = Comment(
            id = tempId,
            userId = username,
            username = username,
            displayName = context.getString(R.string.you_capitalized),
            profileImageRes = R.drawable.ic_launcher,
            profileImageUrl = _uiState.value.currentUserImageUrl,
            content = content,
            createdAt = System.currentTimeMillis(),
            isOwnComment = true,
            parentId = if (parentId != 0L) parentId.toString() else null
        )
        val before = _uiState.value.comments
        val updatedComments = if (parentId != 0L) {
            val parentIndex = before.indexOfFirst { it.id == parentId.toString() }
            if (parentIndex >= 0) {
                val head = before.subList(0, parentIndex + 1)
                val tail = before.subList(parentIndex + 1, before.size)
                head + listOf(optimistic) + tail
            } else {
                listOf(optimistic) + before
            }
        } else {
            listOf(optimistic) + before
        }

        _uiState.update { it.copy(comments = updatedComments) }

        viewModelScope.launch {
            val result = commentRepository.createComment(
                postId = postId,
                content = content,
                parentId = parentId
            )
            result.fold(
                onSuccess = { created ->
                    PostInteractionEvent.emitCommentEvent(postId, 1)
                    val mapped = Comment(
                        id = created.id.toString(),
                        userId = created.member.memberId.toString(),
                        username = created.member.memberUsername,
                        displayName = created.member.memberName,
                        profileImageRes = R.drawable.ic_launcher,
                        profileImageUrl = created.member.memberImage?.imageUrl,
                        content = created.content,
                        createdAt = created.uploadDate.atZone(ZoneId.systemDefault()).toInstant()
                            .toEpochMilli(),
                        likesCount = created.commentLikesCount,
                        isLiked = created.commentLikeFlag,
                        isOwnComment = true,
                        parentId = if (parentId != 0L) parentId.toString() else null
                    )
                    _uiState.update { state ->
                        state.copy(
                            comments = state.comments.map { if (it.id == tempId) mapped else it },
                            replyParentId = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(comments = before, replyParentId = null) }
                    if (error is ApiError.Unauthorized && error.code == "M018") {
                        _uiState.update { it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = true)) }
                    }
                }
            )
        }
    }

    fun consumeLoginRequiredError() {
        _uiState.update { it.copy(showLoginRequiredError = false) }
    }

    fun dismissEmailNotVerified() {
        _uiState.update { it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = false)) }
    }

    fun verifyEmail(context: Context) {
        _uiState.update {
            it.copy(verifyEmailState = it.verifyEmailState.copy(isSending = true))
        }
        viewModelScope.launch {
            authRepository.verifyEmail().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = true
                            )
                        )
                    }
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.email_sent),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    dismissEmailNotVerified()
                },
                onFailure = { error ->
                    _uiState.update {
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
}


