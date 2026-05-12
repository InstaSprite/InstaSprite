package com.instasprite.app.ui.social.profile.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.social.profile.contract.ProfileImageUiState
import com.instasprite.app.ui.social.profile.contract.UserProfileState
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ProfileInfoSection(
    userProfile: UserProfileState,
    profileImageState: ProfileImageUiState,
    onEditProfileClick: () -> Unit,
    onFollowClick: () -> Unit,
    onEditAvatarClick: () -> Unit = {},
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    isLoggedIn: Boolean = false
) {
    LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(120.dp), // Increased size to accommodate the button
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(AppTheme.colors.BackgroundColorDarker)
                    ) {
                        if (profileImageState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                                color = AppTheme.colors.BottomBarColor,
                                strokeWidth = 2.dp
                            )
                        } else if (profileImageState.imageUrl != null) {

                            AsyncImageView(
                                imageUrl = profileImageState.imageUrl,
                                altText = stringResource(R.string.profile_picture),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher),
                                contentDescription = stringResource(R.string.profile_picture),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Floating edit button - only show for own profile
                if (userProfile.isOwnProfile) {
                    FloatingActionButton(
                        onClick = onEditAvatarClick,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = -6.dp, y = -12.dp), // Position it higher up on the circle
                        containerColor = AppTheme.colors.BottomBarColor,
                        contentColor = AppTheme.colors.TextColorLight,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_avatar),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userProfile.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextColorLight,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        count = userProfile.postsCount,
                        label = stringResource(R.string.posts)
                    )
                    StatItem(
                        count = userProfile.followersCount,
                        label = stringResource(R.string.followers),
                        onClick = onFollowersClick
                    )
                    StatItem(
                        count = userProfile.followingCount,
                        label = stringResource(R.string.following),
                        onClick = onFollowingClick
                    )
                }
            }
        }

        // Bio section
        Text(
            text = if (userProfile.bio.isNotEmpty()) userProfile.bio else stringResource(R.string.no_bio_available),
            fontSize = 14.sp,
            color = AppTheme.colors.TextColorLight,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (userProfile.isOwnProfile) {
                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppTheme.colors.TextColorLight
                    ),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = AppTheme.colors.TextColorLight.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.edit_profile),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (isLoggedIn) {
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userProfile.isFollowing) AppTheme.colors.BackgroundColor else AppTheme.colors.SelectedColor,
                    )
                ) {
                    Text(
                        text = if (userProfile.isFollowing) stringResource(R.string.unfollow) else stringResource(
                            R.string.follow
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (userProfile.isFollowing) AppTheme.colors.AccentButtonColor else AppTheme.colors.TextColorDark
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier.clickable { onClick() }
        } else {
            Modifier
        }
    ) {
        Text(
            text = formatCount(count),
            color = AppTheme.colors.TextColorLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 1_000_000)}M"
        count >= 1_000 -> "${(count / 1_000)}K"
        else -> count.toString()
    }
}