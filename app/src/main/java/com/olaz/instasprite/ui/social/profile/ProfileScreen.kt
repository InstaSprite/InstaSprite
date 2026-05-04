package com.olaz.instasprite.ui.social.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.social.profile.component.EmptyStateContent
import com.olaz.instasprite.ui.social.profile.component.ProfileHeader
import com.olaz.instasprite.ui.social.profile.component.ProfileInfoSection
import com.olaz.instasprite.ui.social.profile.component.ProfileTabRow
import com.olaz.instasprite.ui.social.profile.contract.ProfileContentState
import com.olaz.instasprite.ui.social.profile.contract.ProfileScreenEvent
import com.olaz.instasprite.ui.social.profile.contract.ProfileTab
import com.olaz.instasprite.ui.social.profile.contract.UserProfileState
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.ui.social.session.SocialSessionViewModel
import com.olaz.instasprite.ui.social.profile.dialog.EditProfileDialog
import com.olaz.instasprite.ui.social.profile.dialog.FollowersDialog
import com.olaz.instasprite.ui.social.profile.dialog.FollowingDialog
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.UiUtils
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
            viewModel.loadProfileImage(null)
        } else {
            viewModel.loadUserProfile(userId)
            viewModel.loadProfileImage(userId)
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

    UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColorDarker)

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileContent(
            state = contentState,
            event = ProfileScreenEvent(
                onBackClick = onBackClick,
                onEditProfileClick = { viewModel.toggleEditProfileDialog() },
                onEditAvatarClick = { viewModel.toggleEditAvatarDialog() },
                onFollowClick = { viewModel.toggleFollow() },
                onFollowersClick = { viewModel.showFollowersDialog() },
                onFollowingClick = { viewModel.showFollowingDialog() },
                onTabSelected = { index -> viewModel.selectTab(index) },
                onUpdateProfile = { displayName, bio -> viewModel.updateUserProfile(displayName, bio) },
                onFollowUser = { id -> viewModel.followUser(id) },
                onUnfollowUser = { id -> viewModel.unfollowUser(id) },
                onUploadAvatar = { uri -> viewModel.uploadProfileImage(uri) },
                onPostClick = { postId -> onPostClick?.invoke(postId) },
                onMenuClick = { onMenuClick?.invoke() },
                onClearError = { viewModel.clearError() },
                onDismissEditProfile = { viewModel.toggleEditProfileDialog() },
                onDismissAvatarDialog = { viewModel.toggleEditAvatarDialog() },
                onDismissFollowers = { viewModel.hideFollowersDialog() },
                onDismissFollowing = { viewModel.hideFollowingDialog() },
                onConsumeLoginRequiredError = { viewModel.consumeLoginRequiredError() }
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
                containerColor = CatppuccinUI.BackgroundColorDarker,
                dismissActionContentColor = CatppuccinUI.DismissButtonColor

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
            CircularProgressIndicator(color = CatppuccinUI.BottomBarColor)
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
            .background(CatppuccinUI.BackgroundColorDarker)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
        ) {
            ProfileInfoSection(
                userProfile = state.userProfile,
                profileImageState = state.profileImageUiState,
                onEditProfileClick = event.onEditProfileClick,
                onFollowClick = event.onFollowClick,
                onEditAvatarClick = event.onEditAvatarClick,
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
                                        .background(CatppuccinUI.Foreground2Color)
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
                                    colors = CardDefaults.cardColors(containerColor = CatppuccinUI.BackgroundColor),
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
                                                        .background(CatppuccinUI.Foreground2Color)
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
                                                        color = CatppuccinUI.TextColorLight,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = formatTimeAgoLocal(post.postUploadDate),
                                                        color = CatppuccinUI.Subtext0Color,
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

    // Dialogs
    if (state.showEditProfileDialog) {
        EditProfileDialog(
            userProfile = state.userProfile,
            onDismiss = event.onDismissEditProfile,
            onSave = { newDisplayName, newBio ->
                event.onUpdateProfile(newDisplayName, newBio)
                event.onDismissEditProfile()
            }
        )
    }

    if (state.showEditAvatarDialog) {
        var pendingAvatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri -> if (uri != null) pendingAvatarUri = uri }

        AlertDialog(
            onDismissRequest = {
                event.onDismissAvatarDialog()
                pendingAvatarUri = null
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        event.onDismissAvatarDialog()
                        pendingAvatarUri = null
                    }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            pendingAvatarUri?.let { uri ->
                                event.onUploadAvatar(uri)
                                pendingAvatarUri = null
                            }
                        },
                        enabled = pendingAvatarUri != null && !state.profileImageUiState.isLoading
                    ) {
                        if (state.profileImageUiState.isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = CatppuccinUI.TextColorLight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.uploading))
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.confirm),
                                color = CatppuccinUI.TextColorDark
                            )
                        }
                    }
                }
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.change_profile_photo))
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val previewSource = pendingAvatarUri ?: state.profileImageUiState.imageUrl
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(CatppuccinUI.Foreground2Color)
                    ) {
                        AsyncImageView(
                            imageUrl = previewSource.toString(),
                            altText = stringResource(R.string.avatar_preview),
                            modifier = Modifier
                                .matchParentSize()
                                .clip(androidx.compose.foundation.shape.CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable(enabled = !state.profileImageUiState.isLoading) {
                                    if (!state.profileImageUiState.isLoading) imagePicker.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.pick_image),
                                tint = CatppuccinUI.TextColorLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
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

