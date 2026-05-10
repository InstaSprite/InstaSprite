package com.olaz.instasprite.ui.social.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.social.search.component.MemberSearchResults
import com.olaz.instasprite.ui.social.search.component.PostSearchResults
import com.olaz.instasprite.ui.social.search.component.TrendingSection
import com.olaz.instasprite.ui.theme.AppTheme
import java.time.LocalDateTime

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenHashtag: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    SearchScreenContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::onSearch,
        onClearSearch = viewModel::clearSearch,
        onBackClick = onBackClick,
        onOpenProfile = onOpenProfile,
        onOpenComments = onOpenComments,
        onOpenHashtag = onOpenHashtag
    )
}

@Composable
fun SearchScreenContent(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onBackClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenHashtag: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.BackgroundColorDarker),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = AppTheme.colors.TextColorLight
                )
            }

            TextField(
                value = state.query,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        text = "Search posts, @users, #hashtags...",
                        color = AppTheme.colors.Subtext0Color
                    )
                },
                singleLine = true,
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = AppTheme.colors.DismissButtonColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.colors.BackgroundColorDarker,
                    disabledContainerColor = AppTheme.colors.BackgroundColorDarker,
                    unfocusedContainerColor = AppTheme.colors.BackgroundColorDarker,
                    focusedTextColor = AppTheme.colors.TextColorLight,
                    unfocusedTextColor = AppTheme.colors.TextColorLight,
                    cursorColor = AppTheme.colors.TextColorLight,
                    focusedBorderColor = AppTheme.colors.WarningColor,
                    unfocusedBorderColor = AppTheme.colors.WarningColor,
                    unfocusedPlaceholderColor = AppTheme.colors.WarningColor,
                    focusedPlaceholderColor = AppTheme.colors.Subtext0Color
                )
            )
        }

        when {
            state.isSearching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
                }
            }

            state.hasSearched && state.searchType == "MEMBER" -> {
                MemberSearchResults(
                    members = state.searchResultMembers,
                    onOpenProfile = onOpenProfile
                )
            }

            state.hasSearched -> {
                PostSearchResults(
                    posts = state.searchResultPosts,
                    onOpenComments = onOpenComments
                )
            }

            else -> {
                TrendingSection(
                    isLoading = state.isLoadingTrending,
                    posts = state.trendingPosts,
                    onOpenComments = onOpenComments
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun SearchScreenContentPreview() {
    val samplePosts = listOf(
        PostData(
            postId = 1,
            postContent = "My pixel art character #pixelart",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(1, "artist1", "Pixel Artist", null),
            postCommentsCount = 5,
            postLikesCount = 42,
            postBookmarkFlag = false,
            postLikeFlag = true,
            commentOptionFlag = true,
            isFollowing = false
        ),
        PostData(
            postId = 2,
            postContent = "Game dev progress #gamedev",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(2, "dev99", "Game Dev", null),
            postCommentsCount = 12,
            postLikesCount = 128,
            postBookmarkFlag = false,
            postLikeFlag = false,
            commentOptionFlag = true,
            isFollowing = true
        )
    )

    SearchScreenContent(
        state = SearchUiState(trendingPosts = samplePosts),
        onQueryChanged = {},
        onSearch = {},
        onClearSearch = {},
        onBackClick = {},
        onOpenProfile = {},
        onOpenComments = {},
        onOpenHashtag = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun SearchScreenSearchingPreview() {
    SearchScreenContent(
        state = SearchUiState(query = "pixel", isSearching = true, hasSearched = true),
        onQueryChanged = {},
        onSearch = {},
        onClearSearch = {},
        onBackClick = {},
        onOpenProfile = {},
        onOpenComments = {},
        onOpenHashtag = {}
    )
}
