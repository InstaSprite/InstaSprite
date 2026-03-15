package com.olaz.instasprite.ui.social.feed

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.olaz.instasprite.ui.social.feed.component.PostList
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.feed.contract.FeedScreenEvent
import com.olaz.instasprite.ui.social.feed.dialog.PostFilterDialog
import com.olaz.instasprite.ui.social.feed.dialog.VerifyEmailDialog
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    loginState: Boolean,
    listState: LazyListState,
    onLoginClick: () -> Unit,
    onOpenComments: (commentId: Long) -> Unit,
    onOpenProfile: (profileUserId: String) -> Unit,
) {
    val state by viewModel.contentState.collectAsState(initial = FeedContentState(loginState = loginState))
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.getCurrentProfile()
        viewModel.loadProfileImage()
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.startPolling()
        }
    }

    val event = remember(viewModel, onLoginClick, onOpenComments, onOpenProfile) {
        FeedScreenEvent(
            onLoginClick = onLoginClick,
            onDismissVerifyEmailDialog = viewModel::dismissVerifyDialog,
            onVerifyEmail = viewModel::verifyEmail,
            onDismissPostFilterDialog = viewModel::togglePostFilterDialog,
            onSelectPostFilter = viewModel::setPostFilter,
            onOpenComments = onOpenComments,
            onOpenProfile = onOpenProfile,
            onToggleLike = viewModel::toggleLikePost,
            onToggleBookmark = viewModel::toggleBookmarkPost,
            onToggleFollow = viewModel::toggleFollow,
            onDeletePost = viewModel::deletePost,
            onRefreshed = viewModel::onRefreshed,
            onConsumeRefreshPending = viewModel::consumeRefreshPending,
            onUpdateTopPostId = viewModel::updateTopPostId
        )
    }

    FeedContent(
        state = state.copy(loginState = loginState),
        listState = listState,
        event = event
    )
}

@Composable
fun FeedContent(
    state: FeedContentState,
    listState: LazyListState,
    event: FeedScreenEvent,
) {
    val context = LocalContext.current

    if (state.verifyEmailState.showVerifyDialog) {
        VerifyEmailDialog(
            verifyEmailState = state.verifyEmailState,
            onDismiss = event.onDismissVerifyEmailDialog,
            onConfirm = {
                event.onVerifyEmail(context)
            }
        )
    }

    if (state.uiState.showPostFilterDialog) {
        PostFilterDialog(
            onDismiss = event.onDismissPostFilterDialog,
            onFilterSelected = event.onSelectPostFilter,
            currentFilter = state.uiState.postFilter
        )
    }

    if (state.loginState) {
        PostList(
            state = state,
            event = event,
            lazyListState = listState
        )
    } else {
        LoginRequiredScreen(onLoginClick = event.onLoginClick)
    }
}

@Preview
@Composable
private fun FeedContentLoggedOutPreview() {
    InstaSpriteTheme {
        FeedContent(
            state = FeedContentState(loginState = false),
            listState = LazyListState(),
            event = FeedScreenEvent(
                onLoginClick = {},
                onDismissVerifyEmailDialog = {},
                onVerifyEmail = {},
                onDismissPostFilterDialog = {},
                onSelectPostFilter = {},
                onOpenComments = {},
                onOpenProfile = {},
                onToggleLike = { _, _ -> },
                onToggleBookmark = { _, _ -> },
                onToggleFollow = { _, _ -> },
                onDeletePost = {},
                onRefreshed = {},
                onConsumeRefreshPending = {},
                onUpdateTopPostId = {}
            )
        )
    }
}

@Preview
@Composable
private fun FeedContentLoggedInPreview() {
    InstaSpriteTheme {
        FeedContent(
            state = FeedContentState(loginState = true),
            listState = LazyListState(),
            event = FeedScreenEvent(
                onLoginClick = {},
                onDismissVerifyEmailDialog = {},
                onVerifyEmail = {},
                onDismissPostFilterDialog = {},
                onSelectPostFilter = {},
                onOpenComments = {},
                onOpenProfile = {},
                onToggleLike = { _, _ -> },
                onToggleBookmark = { _, _ -> },
                onToggleFollow = { _, _ -> },
                onDeletePost = {},
                onRefreshed = {},
                onConsumeRefreshPending = {},
                onUpdateTopPostId = {}
            )
        )
    }
}
