package com.instasprite.app.ui.social.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.instasprite.app.ui.social.session.SocialSessionState
import com.instasprite.app.ui.social.session.SocialSessionViewModel
import com.instasprite.app.ui.social.profile.dialog.FollowersDialog
import com.instasprite.app.ui.social.profile.dialog.FollowingDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        if (userId == null) {
            viewModel.loadCurrentUserProfile()
        } else {
            viewModel.loadUserProfile(userId)
        }
    }

    LaunchedEffect(contentState.errorMessage) {
        contentState.errorMessage?.let { errorMessage ->
            Toast.makeText(
                context,
                "${context.getString(R.string.error)}: $errorMessage",
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearError()
        }
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(contentState.showLoginRequiredError) {
        if (contentState.showLoginRequiredError) {
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
}

@Composable
fun ProfileContent(
    state: ProfileContentState,
    event: ProfileScreenEvent,
    isLoggedIn: Boolean
) {
    LocalContext.current

    if (state.isLoading && state.userProfile.username.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppTheme.colors.BottomBarColor)
        }
        return
    }

    val lazyListState = rememberLazyListState()
    val staggeredState = rememberLazyStaggeredGridState()

    val isOwnProfile = state.userProfile.isOwnProfile
    val displayedTabs = if (isOwnProfile) ProfileTab.values() else arrayOf(ProfileTab.POSTS)
    val safeIndex = state.selectedTabIndex.coerceAtMost(displayedTabs.lastIndex)
    val selectedTab = displayedTabs[safeIndex]

    val currentPosts = when (selectedTab) {
        ProfileTab.POSTS -> state.userPosts
        ProfileTab.SAVED -> state.sharedPosts
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
        ) {
            ProfileInfoSection(
                userProfile = state.userProfile,
                onEditProfileClick = event.onEditProfileClick,
                onFollowClick = event.onFollowClick,
                onFollowersClick = event.onFollowersClick,
                onFollowingClick = event.onFollowingClick,
                isLoggedIn = isLoggedIn
            )

            ProfileTabRow(
                selectedTabIndex = safeIndex,
                tabs = displayedTabs,
                onTabSelected = event.onTabSelected
            )

            if (currentPosts.isEmpty()) {
                val emptyStateMessage = when (selectedTab) {
                    ProfileTab.POSTS -> if (isOwnProfile) stringResource(R.string.create_your_first_sprite) else stringResource(
                        R.string.no_posts_to_show
                    )

                    ProfileTab.SAVED -> stringResource(R.string.posts_you_save_will_appear_here)
                }

                val emptyStateTitle = when (selectedTab) {
                    ProfileTab.POSTS -> stringResource(R.string.no_posts_yet)
                    ProfileTab.SAVED -> stringResource(R.string.no_saved_posts)
                }

                val emptyStateIcon = when (selectedTab) {
                    ProfileTab.POSTS -> Icons.Default.AccountBox
                    ProfileTab.SAVED -> Icons.Outlined.Share
                }

                EmptyStateContent(
                    icon = emptyStateIcon,
                    title = emptyStateTitle,
                    subtitle = emptyStateMessage
                )
            } else {
                when (selectedTab) {
                    ProfileTab.POSTS -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
                            state = staggeredState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp
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
                                        .fillMaxSize()
                                        .background(AppTheme.colors.Foreground2Color)
                                        .clickable { event.onPostClick(post.postId) },
                                )
                            }
                        }
                    }

                    ProfileTab.SAVED -> {
                        androidx.compose.foundation.lazy.LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = currentPosts,
                                key = { it.postId }
                            ) { post ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.BackgroundColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .clickable { event.onPostClick(post.postId) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                                        .background(AppTheme.colors.Foreground2Color)
                                                ) {
                                                    AsyncImageView(
                                                        imageUrl = post.member.memberImage?.imageUrl
                                                            ?: "",
                                                        altText = stringResource(R.string.profile_image),
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = post.member.memberUsername,
                                                        color = AppTheme.colors.TextColorLight,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = formatTimeAgoLocal(post.postUploadDate),
                                                        color = AppTheme.colors.Subtext0Color,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val firstImage = post.postImages.firstOrNull()
                                        AsyncImageView(
                                            imageUrl = firstImage?.postImageUrl ?: "",
                                            altText = firstImage?.altText
                                                ?: stringResource(R.string.post_image),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ProfileHeader(
            username = state.userProfile.username,
            isOwnProfile = isOwnProfile,
            onBackClick = event.onBackClick,
            onMenuClick = event.onMenuClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }

    if (state.showFollowersDialog) {
        FollowersDialog(
            followers = state.followers,
            isLoading = state.followersLoading,
            onDismiss = event.onDismissFollowers,
            onFollowClick = event.onFollowUser,
            onProfileClick = { username ->
                TODO()
            }
        )
    }

    if (state.showFollowingDialog) {
        FollowingDialog(
            following = state.following,
            isLoading = state.followingLoading,
            onDismiss = event.onDismissFollowing,
            onUnfollowClick = event.onUnfollowUser,
            onProfileClick = { username ->
                TODO()
            }
        )
    }
}

private fun formatTimeAgoLocal(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)
    return when {
        duration.toMinutes() < 1 -> "now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
        duration.toHours() < 24 -> "${duration.toHours()}h"
        duration.toDays() < 7 -> "${duration.toDays()}d"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
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
                    bio = "I am",
                    postsCount = 10,
                    followersCount = 100,
                    followingCount = 150
                )
            ),
            event = ProfileScreenEvent(),
            isLoggedIn = true
        )
    }
}

