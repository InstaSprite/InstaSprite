package com.instasprite.app.ui.social.profile.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.social.profile.contract.UserProfileState
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun ProfileInfoSection(
    userProfile: UserProfileState,
    compressionProgress: Float = 0f,
    onEditProfileClick: () -> Unit,
    onFollowClick: () -> Unit,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    isLoggedIn: Boolean = false
) {
    val avatarContainerSize = lerp(120.dp, 64.dp, compressionProgress)
    val avatarSize = lerp(100.dp, 52.dp, compressionProgress)
    val fadeAlpha = lerp(1f, 0f, (compressionProgress * 2f).coerceAtMost(1f))
    val bioBoxHeight = lerp(60.dp, 0.dp, compressionProgress)
    val buttonBoxHeight = lerp(56.dp, 0.dp, compressionProgress)

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
                modifier = Modifier.size(avatarContainerSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(AppTheme.colors.BackgroundColorDarker),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileImage(
                            imageUrl = userProfile.profileImageUrl,
                            size = avatarSize
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
                    StatItem(count = userProfile.postsCount, label = stringResource(R.string.posts))
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bioBoxHeight)
                .clipToBounds()
                .graphicsLayer { alpha = fadeAlpha }
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (userProfile.bio.isNotEmpty()) userProfile.bio
                    else stringResource(R.string.no_bio_available),
                    fontSize = 14.sp,
                    color = AppTheme.colors.TextColorLight,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonBoxHeight)
                .clipToBounds()
                .graphicsLayer { alpha = fadeAlpha }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
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
                            color = AppTheme.colors.Foreground2Color
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
                            containerColor = if (userProfile.isFollowing)
                                AppTheme.colors.BackgroundColor
                            else
                                AppTheme.colors.SelectedColor,
                        )
                    ) {
                        Text(
                            text = if (userProfile.isFollowing)
                                stringResource(R.string.unfollow)
                            else
                                stringResource(R.string.follow),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (userProfile.isFollowing)
                                AppTheme.colors.AccentButtonColor
                            else
                                AppTheme.colors.TextColorDark
                        )
                    }
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
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = formatCount(count),
            color = AppTheme.colors.TextColorLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AppTheme.colors.Foreground2Color,
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

@Preview(showBackground = true)
@Composable
private fun ProfileInfoSectionPreview() {
    InstaSpriteTheme {
        ProfileInfoSection(
            userProfile = UserProfileState(
                username = "johndoe",
                displayName = "John Doe",
                bio = "Making pixel art one sprite at a time.",
                postsCount = 42,
                followersCount = 1200,
                followingCount = 300,
                isOwnProfile = true
            ),
            compressionProgress = 0f,
            onEditProfileClick = {},
            onFollowClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileInfoSectionCompressedPreview() {
    InstaSpriteTheme {
        ProfileInfoSection(
            userProfile = UserProfileState(
                username = "johndoe",
                displayName = "John Doe",
                bio = "Making pixel art one sprite at a time.",
                postsCount = 42,
                followersCount = 1200,
                followingCount = 300,
                isOwnProfile = true
            ),
            compressionProgress = 1f,
            onEditProfileClick = {},
            onFollowClick = {}
        )
    }
}