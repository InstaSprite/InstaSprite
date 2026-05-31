package com.instasprite.app.ui.social.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.instasprite.app.R
import com.instasprite.app.domain.model.NotificationType
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.social.notification.composable.NotificationItem
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToPost: (Long) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyPagingItems = viewModel.pagedNotifications.collectAsLazyPagingItems()

    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.notifications),
                onBackClick = onBackClick,
                actions = {
                    if (lazyPagingItems.itemCount > 0) {
                        Button(
                            onClick = {
                                viewModel.markAllAsRead()
                                lazyPagingItems.refresh()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.AccentButtonColor
                            ),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(stringResource(R.string.mark_all_as_read), color = AppTheme.colors.TextColorDark)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.colors.BackgroundColor)
        ) {
            if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.SelectedColor
                )
            } else if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.NotLoading) {
                Text(
                    text = stringResource(R.string.no_notifications_yet),
                    color = AppTheme.colors.Foreground2Color,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val pullRefreshState = rememberPullToRefreshState()
                val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { lazyPagingItems.refresh() },
                    state = pullRefreshState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = isRefreshing,
                            containerColor = AppTheme.colors.BackgroundColor,
                            color = AppTheme.colors.SelectedColor,
                            state = pullRefreshState
                        )
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(lazyPagingItems.itemCount) { index ->
                            val notification = lazyPagingItems[index]
                            if (notification != null) {
                                NotificationItem(
                                    notification = notification,
                                    onClick = {
                                        if (!notification.isRead) {
                                            viewModel.markAsRead(notification)
                                        }
                                        val entityId = notification.relatedEntityId
                                        if (entityId != null) {
                                            when (notification.type) {
                                                NotificationType.FOLLOW -> {
                                                    val username =
                                                        notification.recentActors.firstOrNull()?.username
                                                    if (!username.isNullOrEmpty()) {
                                                        onNavigateToProfile(username)
                                                    }
                                                }

                                                NotificationType.LIKE, NotificationType.MENTION, NotificationType.COMMENT -> {
                                                    val postId = entityId.toLongOrNull()
                                                    if (postId != null) {
                                                        onNavigateToPost(postId)
                                                    }
                                                }

                                                NotificationType.UNKNOWN -> {}
                                            }
                                        }
                                    }
                                )
                                HorizontalDivider(
                                    color = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }

                        if (lazyPagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.pixelDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

