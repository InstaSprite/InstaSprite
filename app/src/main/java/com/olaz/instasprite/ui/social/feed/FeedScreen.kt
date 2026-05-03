package com.olaz.instasprite.ui.social.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.olaz.instasprite.ui.components.composable.MaintenanceScreen
import com.olaz.instasprite.ui.social.feed.component.PostList
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.feed.contract.FeedScreenEvent
import com.olaz.instasprite.ui.social.feed.dialog.PostFilterDialog
import com.olaz.instasprite.ui.social.feed.dialog.VerifyEmailDialog
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.ui.social.session.SocialSessionViewModel
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    sessionViewModel: SocialSessionViewModel = hiltViewModel(),
    listState: LazyListState,
    onLoginClick: () -> Unit,
    onOpenComments: (commentId: Long) -> Unit,
    onOpenProfile: (profileUserId: String) -> Unit,
) {
    val state by viewModel.contentState.collectAsState(initial = FeedContentState())
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isLoggedIn = sessionState is SocialSessionState.LoggedIn
    val isOnline by viewModel.isOnline.collectAsState()

    LaunchedEffect(viewModel, lifecycle) {
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
            onUpdateTopPostId = viewModel::updateTopPostId,
            onOpenHashtag = {},
            onClearError = viewModel::clearError,
            onRetryConnection = viewModel::retryConnection
        )
    }

    FeedContent(
        isLoggedIn = isLoggedIn,
        isOnline = isOnline,
        state = state,
        listState = listState,
        event = event
    )
}

@Composable
fun FeedContent(
    isLoggedIn: Boolean,
    isOnline: Boolean = true,
    state: FeedContentState,
    listState: LazyListState,
    event: FeedScreenEvent,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.profileState.error, state.profileImageState.error) {
        val error = state.profileState.error ?: state.profileImageState.error
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            event.onClearError()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isServerMaintenance -> MaintenanceScreen(
                onReload = event.onRetryConnection
            )

            isLoggedIn -> PostList(state = state, event = event, lazyListState = listState)
            else -> LoginRequiredScreen(onLoginClick = event.onLoginClick)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview
@Composable
private fun FeedContentLoggedOutPreview() {
    InstaSpriteTheme {
        FeedContent(
            isLoggedIn = false,
            state = FeedContentState(),
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
                onUpdateTopPostId = {},
                onOpenHashtag = {},
                onClearError = {},
                onRetryConnection = {}
            )
        )
    }
}

@Preview
@Composable
private fun FeedContentLoggedInPreview() {
    InstaSpriteTheme {
        FeedContent(
            isLoggedIn = true,
            state = FeedContentState(),
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
                onUpdateTopPostId = {},
                onOpenHashtag = {},
                onClearError = {},
                onRetryConnection = {}
            )
        )
    }
}

@Preview
@Composable
private fun FeedContentMaintenancePreview() {
    InstaSpriteTheme {
        FeedContent(
            isLoggedIn = true,
            isOnline = false,
            state = FeedContentState(),
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
                onUpdateTopPostId = {},
                onOpenHashtag = {},
                onClearError = {},
                onRetryConnection = {}
            )
        )
    }
}
