package com.instasprite.app.ui.social.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.social.profile.component.EmptyStateContent
import com.instasprite.app.ui.social.profile.component.ProfileHeader
import com.instasprite.app.ui.social.profile.component.ProfileInfoSection
import com.instasprite.app.ui.social.profile.component.ProfileTabRow
import com.instasprite.app.ui.social.profile.contract.ProfileContentState
import com.instasprite.app.ui.social.profile.contract.ProfileScreenEvent
import com.instasprite.app.ui.social.profile.contract.ProfileTab
import com.instasprite.app.ui.social.profile.contract.UserProfileState
import com.instasprite.app.ui.social.profile.dialog.FollowersDialog
import com.instasprite.app.ui.social.profile.dialog.FollowingDialog
import com.instasprite.app.ui.social.session.SocialSessionState
import com.instasprite.app.ui.social.session.SocialSessionViewModel
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ProfileScreen(
    userId: String? = null,
    onBackClick: () -> Unit = {},
    onPostClick: ((postId: Long) -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onLoginClick: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileScreenViewModel = hiltViewModel(),
    sessionViewModel: SocialSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contentState by viewModel.contentState.collectAsState()
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val isLoggedIn = sessionState is SocialSessionState.LoggedIn

    LaunchedEffect(userId) {
        if (userId == null) viewModel.loadCurrentUserProfile()
        else viewModel.loadUserProfile(userId)
    }

    LaunchedEffect(contentState.errorMessage) {
        contentState.errorMessage?.let {
            Toast.makeText(context, "${context.getString(R.string.error)}: $it", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(contentState.showLoginRequiredError) {
        if (contentState.showLoginRequiredError) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.login_required),
                actionLabel = context.getString(R.string.login),
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) onLoginClick()
            viewModel.consumeLoginRequiredError()
        }
    }

    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileContent(
            state = contentState,
            event = ProfileScreenEvent(
                onBackClick = onBackClick,
                onEditProfileClick = onNavigateToEditProfile,
                onFollowClick = { viewModel.toggleFollow() },
                onFollowersClick = { viewModel.showFollowersDialog() },
                onFollowingClick = { viewModel.showFollowingDialog() },
                onTabSelected = { index -> viewModel.selectTab(index) },
                onFollowUser = { id -> viewModel.followUser(id) },
                onUnfollowUser = { id -> viewModel.unfollowUser(id) },
                onPostClick = { postId -> onPostClick?.invoke(postId) },
                onMenuClick = { onMenuClick?.invoke() },
                onClearError = { viewModel.clearError() },
                onDismissFollowers = { viewModel.hideFollowersDialog() },
                onDismissFollowing = { viewModel.hideFollowingDialog() },
                onConsumeLoginRequiredError = { viewModel.consumeLoginRequiredError() },
                onNavigateToEditProfile = onNavigateToEditProfile
            ),
            isLoggedIn = isLoggedIn
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable { snackbarHostState.currentSnackbarData?.dismiss() }
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppTheme.colors.BackgroundColorDarker,
                dismissActionContentColor = AppTheme.colors.DismissButtonColor
            )
        }
    }
}

@Composable
fun ProfileContent(
    state: ProfileContentState,
    event: ProfileScreenEvent,
    isLoggedIn: Boolean
) {
    if (state.isLoading && state.userProfile.username.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppTheme.colors.BottomBarColor)
        }
        return
    }

    val lazyListState = rememberLazyListState()

    val profileHeaderHeightPx = with(LocalDensity.current) { 220.dp.toPx() }

    val isOwnProfile = state.userProfile.isOwnProfile
    val displayedTabs = if (isOwnProfile) ProfileTab.values() else arrayOf(ProfileTab.POSTS)
    val safeIndex = state.selectedTabIndex.coerceAtMost(displayedTabs.lastIndex)
    val selectedTab = displayedTabs[safeIndex]

    val currentPosts = when (selectedTab) {
        ProfileTab.POSTS -> state.userPosts
        ProfileTab.SAVED -> state.sharedPosts
    }

    val gridHeight = ((currentPosts.size + 1) / 2 * 200 + 32).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
    ) {
        ProfileHeader(
            username = state.userProfile.username,
            isOwnProfile = isOwnProfile,
            onBackClick = event.onBackClick,
            onMenuClick = event.onMenuClick,
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ProfileInfoSection(
                    userProfile = state.userProfile,
                    compressionProgress = 0f,
                    onEditProfileClick = event.onEditProfileClick,
                    onFollowClick = event.onFollowClick,
                    onFollowersClick = event.onFollowersClick,
                    onFollowingClick = event.onFollowingClick,
                    isLoggedIn = isLoggedIn
                )
            }

            stickyHeader {
                ProfileTabRow(
                    selectedTabIndex = safeIndex,
                    tabs = displayedTabs,
                    onTabSelected = event.onTabSelected
                )
            }

            item {
                if (currentPosts.isEmpty()) {
                    val emptyTitle = when (selectedTab) {
                        ProfileTab.POSTS -> stringResource(R.string.no_posts_yet)
                        ProfileTab.SAVED -> stringResource(R.string.no_saved_posts)
                    }
                    val emptyMessage = when (selectedTab) {
                        ProfileTab.POSTS -> if (isOwnProfile)
                            stringResource(R.string.create_your_first_sprite)
                        else
                            stringResource(R.string.no_posts_to_show)
                        ProfileTab.SAVED -> stringResource(R.string.posts_you_save_will_appear_here)
                    }
                    val emptyIcon = when (selectedTab) {
                        ProfileTab.POSTS -> Icons.Default.AccountBox
                        ProfileTab.SAVED -> Icons.Outlined.Share
                    }
                    EmptyStateContent(
                        icon = emptyIcon,
                        title = emptyTitle,
                        subtitle = emptyMessage
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        userScrollEnabled = false
                    ) {
                        items(
                            items = currentPosts,
                            key = { it.postId }
                        ) { post ->
                            val firstImage = post.postImages.firstOrNull()
                            AsyncImageView(
                                imageUrl = firstImage?.postImageUrl ?: "",
                                altText = firstImage?.altText
                                    ?: stringResource(R.string.post_image),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .fillMaxWidth()
                                    .background(AppTheme.colors.Foreground2Color)
                                    .clickable { event.onPostClick(post.postId) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showFollowersDialog) {
        FollowersDialog(
            followers = state.followers,
            isLoading = state.followersLoading,
            onDismiss = event.onDismissFollowers,
            onFollowClick = event.onFollowUser,
            onProfileClick = { _ -> }
        )
    }

    if (state.showFollowingDialog) {
        FollowingDialog(
            following = state.following,
            isLoading = state.followingLoading,
            onDismiss = event.onDismissFollowing,
            onUnfollowClick = event.onUnfollowUser,
            onProfileClick = { _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    InstaSpriteTheme {
        ProfileContent(
            state = ProfileContentState(
                userProfile = UserProfileState(
                    username = "johndoe",
                    displayName = "John Doe",
                    bio = "Making pixel art one sprite at a time.",
                    postsCount = 10,
                    followersCount = 1200,
                    followingCount = 150
                )
            ),
            event = ProfileScreenEvent(),
            isLoggedIn = true
        )
    }
}
