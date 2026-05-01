package com.olaz.instasprite.ui.social.hashtag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.olaz.instasprite.ui.social.feed.component.PostList
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.feed.contract.FeedScreenEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagFeedScreen(
    viewModel: HashtagFeedViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenHashtag: (String) -> Unit
) {
    val listState = rememberLazyListState()
    
    // dummy FeedContentState to reuse the PostList component
    val dummyState = FeedContentState(
        pagedPosts = viewModel.pagedPosts
    )
    
    // dummy event handler, only mapping the navigation events
    val dummyEvent = FeedScreenEvent(
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
        onOpenHashtag = onOpenHashtag
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "#${viewModel.hashtag}",
                        color = CatppuccinUI.TextColorLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
                state = dummyState,
                event = dummyEvent,
                lazyListState = listState
            )
        }
    }
}
