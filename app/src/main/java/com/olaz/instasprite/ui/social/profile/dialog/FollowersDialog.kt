package com.olaz.instasprite.ui.social.profile.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.social.profile.contract.FollowerUser
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun FollowersDialog(
    followers: List<FollowerUser>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onFollowClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.BackgroundColorDarker,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.followers),
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AppTheme.colors.BottomBarColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.loading_followers),
                            color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (followers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_followers),
                        color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(followers) { follower ->
                        FollowerItem(
                            follower = follower,
                            onFollowClick = onFollowClick,
                            onProfileClick = onProfileClick
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.BottomBarColor
                )
            ) {
                Text(
                    text = stringResource(R.string.close),
                    color = AppTheme.colors.TextColorLight
                )
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun FollowerItem(
    follower: FollowerUser,
    onFollowClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable { onProfileClick(follower.username) }
        ) {
            if (follower.profileImageUrl != null) {
                AsyncImageView(
                    imageUrl = follower.profileImageUrl,
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick(follower.username) }
        ) {
            Text(
                text = follower.username,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (follower.displayName.isNotEmpty()) {
                Text(
                    text = follower.displayName,
                    color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (!follower.isFollowing) {
            OutlinedButton(
                onClick = { onFollowClick(follower.id) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.colors.TextColorLight
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = AppTheme.colors.TextColorLight.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = stringResource(R.string.follow_user),
                    fontSize = 12.sp
                )
            }
        }
    }
}
