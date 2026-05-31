package com.instasprite.app.ui.social.profile.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.social.profile.contract.FollowerUser
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun FollowersDialog(
    followers: List<FollowerUser>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onFollowClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    CustomDialog(
        title = stringResource(R.string.followers),
        onDismiss = onDismiss,
        onConfirm = onDismiss,
        confirmButtonText = stringResource(R.string.close),
        dismissButtonText = "",
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.pixelDp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.BottomBarColor
                    )
                    Spacer(modifier = Modifier.height(6.pixelDp))
                    Text(
                        text = stringResource(R.string.loading_followers),
                        color = AppTheme.colors.Foreground2Color,
                        fontSize = 12.sp
                    )
                }
            }
        } else if (followers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.pixelDp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_followers),
                    color = AppTheme.colors.Foreground2Color,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.pixelDp)
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
    }
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
            .padding(vertical = 6.pixelDp, horizontal = 10.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            imageUrl = follower.profileImageUrl,
            modifier = Modifier.clickable { onProfileClick(follower.username) },
            size = 34.pixelDp
        )

        Spacer(modifier = Modifier.width(10.pixelDp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick(follower.username) }
        ) {
            Text(
                text = follower.displayName,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@" + follower.username,
                color = AppTheme.colors.Foreground2Color,
                fontSize = 12.sp
            )

        }

        Spacer(modifier = Modifier.width(10.pixelDp))

        if (!follower.isFollowing) {
            Button(
                onClick = { onFollowClick(follower.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.LinkColor,
                    contentColor = AppTheme.colors.TextColorLight
                ),
                shape = PixelShape(),
            ) {
                Text(
                    text = stringResource(R.string.follow_user),
                    fontSize = 12.sp,
                    color = AppTheme.colors.TextColorDark
                )
            }
        }
    }
}
