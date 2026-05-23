package com.instasprite.app.ui.social.profile.dialog

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.social.profile.contract.FollowerUser
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun FollowingDialog(
    following: List<FollowerUser>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUnfollowClick: (String) -> Unit,
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
                    text = stringResource(R.string.following),
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
                            text = stringResource(R.string.loading_following),
                            color = AppTheme.colors.Foreground2Color,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (following.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_following),
                        color = AppTheme.colors.Foreground2Color,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(following) { user ->
                        FollowingItem(
                            user = user,
                            onUnfollowClick = onUnfollowClick,
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
private fun FollowingItem(
    user: FollowerUser,
    onUnfollowClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            imageUrl = user.profileImageUrl,
            modifier = Modifier.clickable { onProfileClick(user.username) },
            size = 50.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick(user.username) }
        ) {
            Text(
                text = user.username,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (user.displayName.isNotEmpty()) {
                Text(
                    text = user.displayName,
                    color = AppTheme.colors.Foreground2Color,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { onUnfollowClick(user.id) }
                .background(AppTheme.colors.DismissButtonColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.str_res),
                color = AppTheme.colors.DismissButtonColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
