package com.instasprite.app.ui.social.comments.contract

import com.instasprite.app.domain.model.PostData

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val profileImageRes: Int,
    val profileImageUrl: String? = null,
    val content: String,
    val createdAt: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isOwnComment: Boolean = false,
    val parentId: String? = null
)

data class PostAuthor(
    val id: String,
    val username: String,
    val displayName: String,
    val profileImageRes: Int,
    val isFollowing: Boolean = false
)

data class CommentState(
    val isLoading: Boolean = false,
    val isLiked: Boolean = false,
    val likesCount: Int = 0,
    val isBookmarked: Boolean = false,
    val showShareDialog: Boolean = false,
    val showPostOptionDialog: Boolean = false,
    val errorMessage: String? = null,
    val showImagePager: Boolean = false,
    val backendPost: PostData? = null,
    val postAuthor: PostAuthor? = null,
    val isOwnPost: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val currentUserImageUrl: String? = null,
    val replyParentId: Long? = null,
    val showLoginRequiredError: Boolean = false,
    val showEmailNotVerified: Boolean = false
)

data class CommentScreenEvent(
    val onBackClick: () -> Unit,
    val onProfileClick: (String) -> Unit,
    val onToggleLike: () -> Unit,
    val onToggleBookmark: () -> Unit,
    val onToggleFollow: () -> Unit,
    val onToggleCommentLike: (String) -> Unit,
    val onDeleteComment: (String) -> Unit,
    val onAddComment: (String) -> Unit,
    val onStartReply: (Long) -> Unit,
    val onClearReplyTarget: () -> Unit,
    val onZoomImage: (String) -> Unit,
    val onDismissZoom: () -> Unit,
    val onConsumeLoginRequiredError: () -> Unit = {}
)
