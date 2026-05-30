package com.instasprite.app.ui.social.profile.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun FollowingDialog(
    following: List<FollowerUser>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onUnfollowClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    CustomDialog(
        title = stringResource(R.string.following),
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
                    .height(134.pixelDp),
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
                    .height(200.pixelDp)
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
    }
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
            .padding(vertical = 6.pixelDp, horizontal = 10.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            imageUrl = user.profileImageUrl,
            modifier = Modifier.clickable { onProfileClick(user.username) },
            size = 34.pixelDp
        )

        Spacer(modifier = Modifier.width(10.pixelDp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick(user.username) }
        ) {
            Text(
                text = user.displayName,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@" + user.username,
                color = AppTheme.colors.Foreground2Color,
                fontSize = 12.sp
            )

        }

        Spacer(modifier = Modifier.width(10.pixelDp))

        Box(
            modifier = Modifier
                .size(22.pixelDp)
                .clip(PixelShape(3))
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
