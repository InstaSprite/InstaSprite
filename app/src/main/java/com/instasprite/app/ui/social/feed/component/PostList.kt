package com.instasprite.app.ui.social.feed.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.JumpToTopButton
import com.instasprite.app.ui.components.composable.MaintenanceScreen
import com.instasprite.app.ui.social.feed.contract.FeedContentState
import com.instasprite.app.ui.social.feed.contract.FeedScreenEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.toUserMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun PostList(
    state: FeedContentState,
    event: FeedScreenEvent,
    lazyListState: LazyListState,
    isOnline: Boolean,
    isLoggedIn: Boolean
) {
    val pagedItems = state.pagedPosts.collectAsLazyPagingItems()
    val context = LocalContext.current
    val firstItemVisible by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }

    val firstVisiblePostId = pagedItems.itemSnapshotList.items
        .firstOrNull { !state.deletedPostIds.contains(it.postId) }?.postId

    val itemsSnapshot = pagedItems.itemSnapshotList
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(state.refreshPending) {
        if (state.refreshPending) {
            event.onRefreshed()

            pagedItems.refresh()

            snapshotFlow { pagedItems.loadState.refresh }
                .filter { it is LoadState.Loading }
                .first()

            snapshotFlow { pagedItems.loadState.refresh }
                .filter { it !is LoadState.Loading }
                .first()

            lazyListState.scrollToItem(0)

            event.onConsumeRefreshPending()
        }
    }

    LaunchedEffect(itemsSnapshot.items.isNotEmpty()) {
        if (itemsSnapshot.isNotEmpty()) {
            val firstItem = itemsSnapshot.items.firstOrNull()
            if (firstItem != null) {
                event.onUpdateTopPostId(firstItem.postId)
            }
        }
    }

    LaunchedEffect(firstVisiblePostId) {
        if (firstVisiblePostId != null) {
            event.onUpdateTopPostId(firstVisiblePostId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
    ) {
        val isRefreshError = pagedItems.loadState.refresh is LoadState.Error
        val error = if (isRefreshError) (pagedItems.loadState.refresh as LoadState.Error).error else null

        if (pagedItems.loadState.refresh is LoadState.Loading && pagedItems.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
            }
        } else if (isRefreshError && pagedItems.itemCount == 0) {
            if (isOnline) {
                MaintenanceScreen(onReload = { pagedItems.retry() })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "${stringResource(R.string.error)}: ${error?.toUserMessage(context)}",
                        color = AppTheme.colors.TextColorLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (pagedItems.loadState.refresh is LoadState.NotLoading && pagedItems.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_posts_available),
                    color = AppTheme.colors.TextColorLight,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            PullToRefreshBox(
                isRefreshing = pagedItems.loadState.refresh is LoadState.Loading,
                onRefresh = { pagedItems.refresh() },
                state = pullRefreshState,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = pagedItems.loadState.refresh is LoadState.Loading,
                        containerColor = AppTheme.colors.BackgroundColor,
                        color = AppTheme.colors.TextColorLight,
                        state = pullRefreshState
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(
                        count = pagedItems.itemCount,
                        key = pagedItems.itemKey { it.postId }
                    ) { index ->
                        val rawPost = pagedItems[index]

                        if (rawPost != null && !state.deletedPostIds.contains(rawPost.postId)) {
                            val isLiked = state.localLikeState[rawPost.postId] ?: rawPost.postLikeFlag
                            val isBookmarked =
                                state.localBookmarkState[rawPost.postId] ?: rawPost.postBookmarkFlag
                            val targetUser = rawPost.member.memberUsername
                            val isFollowing = state.localFollowState[targetUser] ?: rawPost.isFollowing

                            val originalLikeCount = rawPost.postLikesCount
                            val likeCountDisplay =
                                if (state.localLikeState.containsKey(rawPost.postId)) {
                                    val likedNow = state.localLikeState[rawPost.postId]!!
                                    val likedOriginally = rawPost.postLikeFlag
                                    if (likedNow && !likedOriginally) originalLikeCount + 1
                                    else if (!likedNow && likedOriginally) {
                                        (originalLikeCount - 1).coerceAtLeast(0)
                                    } else {
                                        originalLikeCount
                                    }
                                } else {
                                    originalLikeCount
                                }

                            val delta = state.localCommentState[rawPost.postId] ?: 0
                            val finalCommentCount = (rawPost.postCommentsCount + delta).coerceAtLeast(0)

                            val effectivePost = rawPost.copy(
                                postLikeFlag = isLiked,
                                postBookmarkFlag = isBookmarked,
                                postLikesCount = likeCountDisplay,
                                postCommentsCount = finalCommentCount,
                                isFollowing = isFollowing
                            )

                            val isOwnPost =
                                state.profileState.memberUsername.equals(targetUser, ignoreCase = true)

                            FeedPostItem(
                                post = effectivePost,
                                onPostClick = { event.onOpenComments(rawPost.postId) },
                                onProfileClick = event.onOpenProfile,
                                onHashtagClick = event.onOpenHashtag,
                                onMentionClick = { mention -> event.onOpenProfile(mention.removePrefix("@")) },
                                onFollowClick = { username, following ->
                                    if (!isOwnPost) event.onToggleFollow(username, following)
                                },
                                onLikeClick = { event.onToggleLike(rawPost.postId, isLiked) },
                                onBookmarkClick = {
                                    event.onToggleBookmark(rawPost.postId, isBookmarked)
                                },
                                onCommentClick = { event.onOpenComments(rawPost.postId) },
                                onDeleteClick = event.onDeletePost,
                                modifier = Modifier.padding(vertical = 8.dp),
                                showFollowButton = isLoggedIn && !isOwnPost,
                                showDeleteButton = isOwnPost,
                            )
                        }
                    }

                    if (pagedItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
                            }
                        }
                    }

                    if (pagedItems.loadState.append is LoadState.Error) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { pagedItems.retry() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.SelectedColor)
                                ) {
                                    Text(stringResource(R.string.retry_1), color = AppTheme.colors.TextColorDark)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = firstItemVisible || state.hasNewPosts,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            JumpToTopButton(
                listState = lazyListState,
            )
        }
    }
}