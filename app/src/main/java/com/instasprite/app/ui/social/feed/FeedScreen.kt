package com.instasprite.app.ui.social.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.component.PostList
import com.instasprite.app.ui.social.feed.contract.FeedContentState
import com.instasprite.app.ui.social.feed.contract.FeedScreenEvent
import com.instasprite.app.ui.social.session.SocialSessionState
import com.instasprite.app.ui.social.session.SocialSessionViewModel
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData.mockPagedPosts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            onBottomBarEvent = viewModel::onBottomBarEvent,
            onOpenComments = onOpenComments,
            onOpenProfile = onOpenProfile,
            onToggleLike = viewModel::toggleLikePost,
            onToggleBookmark = viewModel::toggleBookmarkPost,
            onToggleFollow = viewModel::toggleFollow,
            onDeleteClick = { postId -> viewModel.openDialog(FeedDialog.DeletePostConfirm(postId)) },
            onRefreshed = viewModel::onRefreshed,
            onConsumeRefreshPending = viewModel::consumeRefreshPending,
            onUpdateTopPostId = viewModel::updateTopPostId,
            onOpenHashtag = {},
            onClearError = viewModel::clearError,
            onRetryConnection = viewModel::retryConnection,
            onConsumeLoginRequiredError = viewModel::consumeLoginRequiredError
        )
    }

    val feedDialogState by viewModel.dialogState.collectAsState()
    FeedScreenDialogs(feedDialogState, viewModel)

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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.showLoginRequiredError) {
        if (state.showLoginRequiredError) {
            val job = launch {
                delay(5000)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.login_required),
                actionLabel = context.getString(R.string.login),
                withDismissAction = true
            )
            job.cancel()
            if (result == SnackbarResult.ActionPerformed) {
                event.onLoginClick()
            }
            event.onConsumeLoginRequiredError()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PostList(
            state = state,
            event = event,
            lazyListState = listState,
            isOnline = isOnline,
            isLoggedIn = isLoggedIn
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(enabled = true, onClick = {
                    snackbarHostState.currentSnackbarData?.dismiss()
                })
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppTheme.colors.BackgroundColorDarker,
                dismissActionContentColor = AppTheme.colors.DismissButtonColor

            )
        }
    }
}

@Preview
@Composable
private fun FeedContentPreview() {
    InstaSpriteTheme {
        FeedContent(
            isLoggedIn = false,
            state = FeedContentState(pagedPosts = mockPagedPosts),
            listState = LazyListState(),
            event = FeedScreenEvent(
                onLoginClick = {},
                onBottomBarEvent = {},
                onOpenComments = {},
                onOpenProfile = {},
                onToggleLike = { _, _ -> },
                onToggleBookmark = { _, _ -> },
                onToggleFollow = { _, _ -> },
                onDeleteClick = {},
                onRefreshed = {},
                onConsumeRefreshPending = {},
                onUpdateTopPostId = {},
                onOpenHashtag = {},
                onClearError = {},
                onRetryConnection = {},
                onConsumeLoginRequiredError = {}
            )
        )
    }
}
