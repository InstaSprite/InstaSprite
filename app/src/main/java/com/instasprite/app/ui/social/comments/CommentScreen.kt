package com.instasprite.app.ui.social.comments

import com.instasprite.app.utils.pixelDp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.domain.model.PostData
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.social.comments.component.CommentActions
import com.instasprite.app.ui.social.comments.component.CommentInput
import com.instasprite.app.ui.social.comments.component.CommentItem
import com.instasprite.app.ui.social.comments.component.CommentsHeader
import com.instasprite.app.ui.social.comments.contract.Comment
import com.instasprite.app.ui.social.comments.contract.CommentScreenEvent
import com.instasprite.app.ui.social.comments.contract.CommentState
import com.instasprite.app.ui.social.comments.contract.PostAuthor
import com.instasprite.app.ui.social.createpost.composable.TopBar
import com.instasprite.app.ui.social.feed.dialog.VerifyEmailDialog
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
import com.instasprite.app.domain.session.SocialSessionState
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.home.SocialSessionViewModel
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.TimeUtils
import java.time.ZoneId

@Composable
fun CommentScreen(
    postId: Long? = null,
    onBackClick: () -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: CommentViewModel = hiltViewModel(),
    sessionViewModel: SocialSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val isLoggedIn = sessionState is SocialSessionState.LoggedIn
    val context = LocalContext.current
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val commentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(postId) {
        if (postId != null) viewModel.loadPost(postId) else viewModel.clearError()
        viewModel.loadCurrentUserProfile()
    }

    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    val event = remember(viewModel) {
        CommentScreenEvent(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            onToggleLike = viewModel::toggleLike,
            onToggleBookmark = viewModel::toggleBookmark,
            onToggleFollow = viewModel::toggleFollow,
            onToggleCommentLike = viewModel::toggleCommentLike,
            onDeleteComment = viewModel::deleteComment,
            onAddComment = { content ->
                if (content.isNotBlank()) {
                    viewModel.addComment(content)
                    newCommentText = ""
                    focusManager.clearFocus()
                }
            },
            onStartReply = { id ->
                viewModel.startReply(id)
                commentFocusRequester.requestFocus()
            },
            onClearReplyTarget = viewModel::clearReplyTarget,
            onZoomImage = { zoomedImageUrl = it },
            onDismissZoom = { zoomedImageUrl = null },
            onConsumeLoginRequiredError = viewModel::consumeLoginRequiredError
        )
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(uiState.showLoginRequiredError) {
        if (uiState.showLoginRequiredError) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.login_required),
                actionLabel = context.getString(R.string.login),
                true
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onLoginClick()
            }
            viewModel.consumeLoginRequiredError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CommentScreenContent(
            uiState = uiState,
            newCommentText = newCommentText,
            onNewCommentTextChange = { newCommentText = it },
            commentFocusRequester = commentFocusRequester,
            event = event,
            isLoggedIn = isLoggedIn,
            onLoginClick = onLoginClick
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

    // TODO: Implement image zoom overlay if needed

    if (uiState.verifyEmailState.showVerifyDialog) {
        VerifyEmailDialog(
            verifyEmailState = uiState.verifyEmailState,
            onDismiss = viewModel::dismissEmailNotVerified,
            onConfirm = { viewModel.verifyEmail(context) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentScreenContent(
    uiState: CommentState,
    newCommentText: String,
    onNewCommentTextChange: (String) -> Unit,
    commentFocusRequester: FocusRequester,
    event: CommentScreenEvent,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit
) {
    LocalContext.current
    var commentPendingDelete by remember { mutableStateOf<String?>(null) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppTheme.colors.BottomBarColor)
        }
    } else {
        Scaffold(
            topBar = {
                TopBar(
                    onDismiss = event.onBackClick
                )
            },
            bottomBar = {
                if (isLoggedIn) {
                    val replyingToComment =
                        uiState.comments.find { it.id == uiState.replyParentId?.toString() }
                    CommentInput(
                        text = newCommentText,
                        onTextChange = onNewCommentTextChange,
                        onSendClick = { event.onAddComment(newCommentText) },
                        modifier = Modifier.focusRequester(commentFocusRequester),
                        profileImageUrl = uiState.currentUserImageUrl,
                        replyingToUsername = replyingToComment?.username,
                        onCancelReply = event.onClearReplyTarget
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.BackgroundColorDarker)
                            .clickable { onLoginClick() }
                            .padding(10.pixelDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.login_required),
                            color = AppTheme.colors.AccentButtonColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(AppTheme.colors.BackgroundColorDarker)
            ) {
                uiState.backendPost?.let { detail ->
                    item {
                        PostHeader(
                            detail = detail,
                            postAuthor = uiState.postAuthor,
                            isOwnPost = uiState.isOwnPost,
                            onProfileClick = event.onProfileClick,
                            onToggleFollow = event.onToggleFollow,
                            isLoggedIn = isLoggedIn
                        )
                    }

                    if (detail.postContent.isNotBlank()) {
                        item {
                            Text(
                                text = detail.postContent,
                                color = AppTheme.colors.TextColorLight,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.pixelDp)
                            )
                            Spacer(modifier = Modifier.height(6.pixelDp))
                        }
                    }

                    items(detail.postImages) { img ->
                        AsyncImageView(
                            imageUrl = img.postImageUrl,
                            altText = img.altText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.pixelDp, vertical = 2.pixelDp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { event.onZoomImage(img.postImageUrl) }
                        )
                    }
                }

                item {
                    CommentActions(
                        isLiked = uiState.isLiked,
                        isBookmarked = uiState.isBookmarked,
                        likesCount = uiState.likesCount,
                        onLikeClick = event.onToggleLike,
                        onBookmarkClick = event.onToggleBookmark,
                        showBookmark = !uiState.isOwnPost
                    )
                }

                item {
                    CommentsHeader(commentsCount = uiState.comments.size)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.pixelDp),
                        thickness = 1.pixelDp,
                        color = AppTheme.colors.Foreground2Color
                    )
                }

                // AssistChip for reply target is no longer needed here as it is moved to CommentInput

                items(
                    items = uiState.comments,
                    key = { it.id }
                ) { comment ->
                    CommentItem(
                        comment = comment,
                        onProfileClick = event.onProfileClick,
                        onLikeClick = event.onToggleCommentLike,
                        onReplyClick = { id -> event.onStartReply(id.toLongOrNull() ?: 0L) },
                        onDeleteClick = { id -> commentPendingDelete = id }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.pixelDp))
                }
            }
        }

        commentPendingDelete?.let { id ->
            ConfirmationDialog(
                title = stringResource(R.string.delete_comment),
                text = stringResource(R.string.are_you_sure_you_want_to_delete),
                highlightText = stringResource(R.string.delete_comment),
                confirmButtonText = stringResource(R.string.delete),
                dismissButtonText = stringResource(R.string.cancel),
                onConfirm = {
                    event.onDeleteComment(id)
                    commentPendingDelete = null
                },
                onDismiss = { commentPendingDelete = null }
            )
        }
    }
}

@Composable
private fun PostHeader(
    detail: PostData,
    postAuthor: PostAuthor?,
    isOwnPost: Boolean,
    onProfileClick: (String) -> Unit,
    onToggleFollow: () -> Unit,
    isLoggedIn: Boolean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            imageUrl = detail.member.memberImage?.imageUrl,
            modifier = Modifier.size(26.pixelDp),
            size = 26.pixelDp
        )
        Spacer(modifier = Modifier.width(8.pixelDp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = detail.member.memberName,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onProfileClick(postAuthor?.username ?: detail.member.memberUsername)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = TimeUtils.formatTimeAgo(context, detail.postUploadDate),
                color = AppTheme.colors.Foreground2Color,
                fontSize = 12.sp
            )
        }
        if (isLoggedIn && !isOwnPost) {
            IconButton(
                onClick = {
                    if (postAuthor == null) return@IconButton
                    if (postAuthor.isFollowing) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.follow) + " " + postAuthor.displayName,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.unfollow) + " " + postAuthor.displayName,
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    onToggleFollow()
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(14.pixelDp)
            ) {
                PixelIcon(
                    icon = R.drawable.ic_follow,
                    contentDescription = stringResource(R.string.follow),
                    tint = if (postAuthor?.isFollowing == true)
                        AppTheme.colors.AccentButtonColor
                    else
                        AppTheme.colors.Foreground2Color,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentScreenPreview() {
    InstaSpriteTheme {
        CommentScreenContent(
            uiState = CommentState(
                isLoading = false,
                comments = listOf(
                    Comment(
                        id = "1",
                        userId = "user1",
                        username = "johndoe",
                        displayName = "John Doe",
                        profileImageRes = R.drawable.ic_launcher,
                        content = "This is a test comment!",
                        createdAt = System.currentTimeMillis() - 3600000,
                        likesCount = 5,
                        isLiked = true
                    ),
                    Comment(
                        id = "2",
                        userId = "user2",
                        username = "janedoe",
                        displayName = "Jane Doe",
                        profileImageRes = R.drawable.ic_launcher,
                        content = "Love this post!",
                        createdAt = System.currentTimeMillis() - 7200000,
                        likesCount = 2,
                        isLiked = false,
                        parentId = "1"
                    )
                )
            ),
            newCommentText = "",
            onNewCommentTextChange = {},
            commentFocusRequester = remember { FocusRequester() },
            event = CommentScreenEvent(
                onBackClick = {},
                onProfileClick = {},
                onToggleLike = {},
                onToggleBookmark = {},
                onToggleFollow = {},
                onToggleCommentLike = {},
                onDeleteComment = {},
                onAddComment = {},
                onStartReply = {},
                onClearReplyTarget = {},
                onZoomImage = {},
                onDismissZoom = {},
                onConsumeLoginRequiredError = {}
            ),
            isLoggedIn = true,
            onLoginClick = {}
        )
    }
}
