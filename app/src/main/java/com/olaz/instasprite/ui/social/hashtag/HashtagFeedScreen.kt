package com.olaz.instasprite.ui.social.hashtag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.ui.social.feed.component.PostList
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.feed.contract.FeedScreenEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagFeedScreen(
    hashtag: String,
    viewModel: HashtagFeedViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenHashtag: (String) -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(hashtag) {
        viewModel.setHashtag(hashtag)
    }
    
    val currentTag by viewModel.hashtag.collectAsState()

    HashtagFeedScreenContent(
        hashtag = currentTag,
        state = FeedContentState(pagedPosts = viewModel.pagedPosts),
        onBackClick = onBackClick,
        onOpenProfile = onOpenProfile,
        onOpenComments = onOpenComments,
        onOpenHashtag = onOpenHashtag,
        isLoggedIn = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagFeedScreenContent(
    hashtag: String,
    state: FeedContentState,
    onBackClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenHashtag: (String) -> Unit,
    isLoggedIn: Boolean
) {
    val listState = rememberLazyListState()

    val event = FeedScreenEvent(
        onLoginClick = {},
        onDismissVerifyEmailDialog = {},
        onVerifyEmail = {},
        onDismissPostFilterDialog = {},
        onSelectPostFilter = {},
        onOpenComments = onOpenComments,
        onOpenProfile = onOpenProfile,
        onToggleLike = { _, _ -> },
        onToggleBookmark = { _, _ -> },
        onToggleFollow = { _, _ -> },
        onDeletePost = {},
        onRefreshed = {},
        onConsumeRefreshPending = {},
        onUpdateTopPostId = {},
        onOpenHashtag = onOpenHashtag,
        onClearError = {},
        onRetryConnection = {},
        onConsumeLoginRequiredError = {}
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "#$hashtag",
                        color = CatppuccinUI.TextColorLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = CatppuccinUI.TextColorLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CatppuccinUI.BackgroundColorDarker
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CatppuccinUI.BackgroundColorDarker)
                .padding(paddingValues)
        ) {
            PostList(
                state = state,
                event = event,
                lazyListState = listState,
                isOnline = true,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun HashtagFeedScreenContentPreview() {
    HashtagFeedScreenContent(
        hashtag = "pixelart",
        state = FeedContentState(),
        onBackClick = {},
        onOpenProfile = {},
        onOpenComments = {},
        onOpenHashtag = {},
        isLoggedIn = true
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun HashtagFeedScreenContentLongTagPreview() {
    HashtagFeedScreenContent(
        hashtag = "retrogaming",
        state = FeedContentState(),
        onBackClick = {},
        onOpenProfile = {},
        onOpenComments = {},
        onOpenHashtag = {},
        isLoggedIn = true
    )
}
