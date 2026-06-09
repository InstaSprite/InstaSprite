package com.instasprite.app.ui.social.feed.component

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.instasprite.app.R
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.social.profile.contract.FollowerUser
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun PostLikesDialog(
    likes: LazyPagingItems<FollowerUser>,
    onDismiss: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    LocalContext.current

    CustomDialog(
        title = stringResource(R.string.likes),
        onDismiss = onDismiss,
        onConfirm = onDismiss,
        confirmButtonText = stringResource(R.string.close),
        dismissButtonText = "",
    ) {
        if (likes.loadState.refresh is LoadState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.pixelDp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppTheme.colors.BottomBarColor)
            }
        } else if (likes.itemCount == 0 && likes.loadState.refresh is LoadState.NotLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(134.pixelDp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_likes),
                    color = AppTheme.colors.Foreground2Color,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.pixelDp)
            ) {
                items(
                    count = likes.itemCount,
                    key = likes.itemKey { it.id },
                    contentType = likes.itemContentType { "like_member" }
                ) { index ->
                    val follower = likes[index]
                    if (follower != null) {
                        PostLikeItem(
                            follower = follower,
                            onProfileClick = onProfileClick
                        )
                    }
                }

                if (likes.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.pixelDp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppTheme.colors.BottomBarColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostLikeItem(
    follower: FollowerUser,
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
    }
}
