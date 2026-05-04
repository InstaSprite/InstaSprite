package com.olaz.instasprite.ui.social.comments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.social.comments.component.CommentActions
import com.olaz.instasprite.ui.social.comments.component.CommentInput
import com.olaz.instasprite.ui.social.comments.component.CommentItem
import com.olaz.instasprite.ui.social.comments.component.CommentsHeader
import com.olaz.instasprite.ui.social.comments.contract.Comment
import com.olaz.instasprite.ui.social.comments.contract.CommentScreenEvent
import com.olaz.instasprite.ui.social.comments.contract.CommentState
import com.olaz.instasprite.ui.social.comments.contract.PostAuthor
import com.olaz.instasprite.ui.social.createpost.composable.TopBar
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.ui.social.session.SocialSessionViewModel
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.UiUtils
import com.olaz.instasprite.utils.formatTimeAgo
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

    UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColorDarker)

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
                val comment = uiState.comments.find { it.id == id.toString() }
                newCommentText = if (comment != null) "@${comment.username} " else ""
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
                containerColor = CatppuccinUI.BackgroundColorDarker,
                dismissActionContentColor = CatppuccinUI.DismissButtonColor
            )
        }
    }

    // TODO: Implement image zoom overlay if needed
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

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CatppuccinUI.BottomBarColor)
        }
    } else {
        Scaffold(
            topBar = {
//                TopAppBar(
//                    title = {
//                        Text(
//                            text = stringResource(R.string.post),
//                            color = CatppuccinUI.TextColorLight,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = event.onBackClick) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = stringResource(R.string.back),
//                                tint = CatppuccinUI.TextColorLight
//                            )
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = CatppuccinUI.BackgroundColorDarker
//                    )
//                )
                TopBar(
                    onDismiss = event.onBackClick
                )
            },
            bottomBar = {
                if (isLoggedIn) {
                    CommentInput(
                        text = newCommentText,
                        onTextChange = onNewCommentTextChange,
                        onSendClick = { event.onAddComment(newCommentText) },
                        modifier = Modifier.focusRequester(commentFocusRequester),
                        profileImageUrl = uiState.currentUserImageUrl
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CatppuccinUI.BackgroundColorDarker)
                            .clickable { onLoginClick() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.login_required),
                            color = CatppuccinUI.AccentButtonColor,
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
                    .background(CatppuccinUI.BackgroundColorDarker)
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
                                color = CatppuccinUI.TextColorLight,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    items(detail.postImages) { img ->
                        AsyncImageView(
                            imageUrl = img.postImageUrl,
                            altText = img.altText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
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
                }

                if (uiState.replyParentId != null) {
                    item {
                        AssistChip(
                            onClick = event.onClearReplyTarget,
                            label = { Text(text = stringResource(R.string.replying_tap_to_cancel)) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                items(
                    items = uiState.comments,
                    key = { it.id }
                ) { comment ->
                    CommentItem(
                        comment = comment,
                        onProfileClick = event.onProfileClick,
                        onLikeClick = event.onToggleCommentLike,
                        onReplyClick = { id -> event.onStartReply(id.toLongOrNull() ?: 0L) },
                        onDeleteClick = event.onDeleteComment
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
    LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImageView(
            imageUrl = detail.member.memberImage?.imageUrl ?: "",
            altText = stringResource(R.string.profile_picture),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = detail.member.memberUsername,
                color = CatppuccinUI.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onProfileClick(postAuthor?.username ?: detail.member.memberUsername)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatTimeAgo(
                    detail.postUploadDate
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                ),
                color = CatppuccinUI.TextColorLight.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        if (isLoggedIn && !isOwnPost) {
            OutlinedButton(
                onClick = onToggleFollow,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CatppuccinUI.TextColorLight
                ),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = CatppuccinUI.TextColorLight.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (postAuthor?.isFollowing == true) stringResource(R.string.unfollow) else stringResource(
                        R.string.follow
                    ),
                    color = if (postAuthor?.isFollowing == true) CatppuccinUI.AccentButtonColor else CatppuccinUI.TextColorLight,
                    fontSize = 12.sp
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
