package com.instasprite.app.ui.social.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
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
    val listState = rememberLazyListState()

    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.notifications),
                onBackClick = onBackClick,
                actions = {
                    if (uiState.notifications.any { !it.isRead }) {
                        Button(
                            onClick = viewModel::markAllAsRead,
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
            if (uiState.notifications.isEmpty() && uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppTheme.colors.SelectedColor
                )
            } else if (uiState.notifications.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_notifications_yet),
                    color = AppTheme.colors.Foreground2Color,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val pullRefreshState = rememberPullToRefreshState()
                val isRefreshing = uiState.isLoading && uiState.page == 0

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadNotifications(refresh = true) },
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
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                            lastVisibleItem != null && lastVisibleItem.index >= uiState.notifications.size - 5
                        }
                    }

                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value) {
                            viewModel.loadNotifications()
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notifications, key = { it.id }) { notification ->
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

                        if (uiState.isLoading && uiState.page > 0) {
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

