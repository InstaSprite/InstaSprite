package com.olaz.instasprite.ui.social.feed.contract

import android.content.Context
import androidx.paging.PagingData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.social.feed.FeedUiState
import com.olaz.instasprite.ui.social.feed.PostFilter
import com.olaz.instasprite.ui.social.feed.ProfileImageState
import com.olaz.instasprite.ui.social.feed.ProfileState
import com.olaz.instasprite.ui.social.feed.VerifyEmailState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class FeedContentState(
    val uiState: FeedUiState = FeedUiState(),
    val verifyEmailState: VerifyEmailState = VerifyEmailState(),
    val profileImageState: ProfileImageState = ProfileImageState(),
    val profileState: ProfileState = ProfileState(),
    val pagedPosts: Flow<PagingData<PostData>> = emptyFlow(),
    val localLikeState: Map<Long, Boolean> = emptyMap(),
    val localBookmarkState: Map<Long, Boolean> = emptyMap(),
    val localCommentState: Map<Long, Long> = emptyMap(),
    val localFollowState: Map<String, Boolean> = emptyMap(),
    val refreshPending: Boolean = false,
    val hasNewPosts: Boolean = false,
    val deletedPostIds: Set<Long> = emptySet(),
    val isServerMaintenance: Boolean = false
)

data class FeedScreenEvent(
    val onLoginClick: () -> Unit,
    val onDismissVerifyEmailDialog: () -> Unit,
    val onVerifyEmail: (Context) -> Unit,
    val onDismissPostFilterDialog: () -> Unit,
    val onSelectPostFilter: (PostFilter) -> Unit,
    val onOpenComments: (commentId: Long) -> Unit,
    val onOpenProfile: (profileId: String) -> Unit,
    val onToggleLike: (postId: Long, isLike: Boolean) -> Unit,
    val onToggleBookmark: (postId: Long, isBookmark: Boolean) -> Unit,
    val onToggleFollow: (userId: String, isFollow: Boolean) -> Unit,
    val onDeletePost: (postId: Long) -> Unit,
    val onRefreshed: () -> Unit,
    val onConsumeRefreshPending: () -> Unit,
    val onUpdateTopPostId: (Long) -> Unit,
    val onOpenHashtag: (hashtag: String) -> Unit,
    val onClearError: () -> Unit,
    val onRetryConnection: () -> Unit
)
