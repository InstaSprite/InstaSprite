package com.instasprite.app.ui.social.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.ui.social.notification.composable.NotificationItem
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.UiUtils

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
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = AppTheme.colors.TextColorLight
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.notifications),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppTheme.colors.TextColorLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.TopBarColor,
                )
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
                    color = AppTheme.colors.TextColorLight.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id)
                                }
                                val entityId = notification.relatedEntityId
                                if (entityId != null) {
                                    when (notification.type) {
                                        "FOLLOW" -> onNavigateToProfile(entityId)
                                        "LIKE", "MENTION", "COMMENT" -> {
                                            val postId = entityId.toLongOrNull()
                                            if (postId != null) {
                                                onNavigateToPost(postId)
                                            }
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider(
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
                            }
                        }
                    }
                }

                // Simple infinite scroll trigger
                LaunchedEffect(listState.firstVisibleItemIndex) {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    if (lastVisibleItem != null && lastVisibleItem.index >= uiState.notifications.size - 5) {
                        viewModel.loadNotifications()
                    }
                }
            }
        }
    }
}

