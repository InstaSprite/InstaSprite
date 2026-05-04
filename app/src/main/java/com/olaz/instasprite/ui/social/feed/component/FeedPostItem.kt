package com.olaz.instasprite.ui.social.feed.component

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.components.composable.ParsedPostText
import com.olaz.instasprite.ui.social.feed.dialog.DeletePostConfirmDialog
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.toSuffixString
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun FeedPostItem(
    post: PostData,
    onPostClick: () -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onFollowClick: (String, Boolean) -> Unit = { _, _ -> },
    onLikeClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showFollowButton: Boolean = true,
    showDeleteButton: Boolean = false,
) {

    LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        DeletePostConfirmDialog(
            onConfirm = { onDeleteClick(post.postId) },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CatppuccinUI.BackgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPostClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onProfileClick(post.member.memberUsername) }
                ) {
                    AsyncImageView(
                        imageUrl = post.member.memberImage?.imageUrl ?: "",
                        altText = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CatppuccinUI.Foreground2Color),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.member.memberName,
                            color = CatppuccinUI.TextColorLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTimeAgo(post.postUploadDate),
                            color = CatppuccinUI.Subtext0Color,
                            fontSize = 12.sp
                        )
                    }
                }

                if (showFollowButton) {
                    OutlinedButton(
                        onClick = {
                            Log.d(
                                "FeedPostItem",
                                "Follow button clicked for user: ${post.member.memberUsername}, current state: ${post.isFollowing}, new state: ${!post.isFollowing}"
                            )
                            onFollowClick(post.member.memberUsername, !post.isFollowing)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CatppuccinUI.TextColorLight
                        ),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = CatppuccinUI.TextColorLight.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = if (post.isFollowing) stringResource(R.string.unfollow) else stringResource(
                                R.string.follow
                            ),
                            color = if (post.isFollowing) CatppuccinUI.AccentButtonColor else CatppuccinUI.TextColorLight,
                            fontSize = 12.sp
                        )
                    }
                }

                if (showDeleteButton) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CatppuccinUI.DismissButtonColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.postContent.isNotEmpty()) {
                ParsedPostText(
                    text = post.postContent,
                    textColor = CatppuccinUI.TextColorLight,
                    style = CatppuccinTypography.bodyMedium,
                    onHashtagClick = { hashtag -> onHashtagClick(hashtag) },
                    onMentionClick = { mention -> onMentionClick(mention) },
                    onTextClick = { onPostClick() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (post.postImages.isNotEmpty()) {
                post.postImages.forEachIndexed { index, postImage ->

                    val aspectRatio = remember(postImage.postImageUrl) {
                        postImage.imageWidth.toFloat() / postImage.imageHeight.toFloat()
                    }

                    val imageModifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (aspectRatio != null) {
                                it.aspectRatio(aspectRatio)
                            } else {
                                it
                            }
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(CatppuccinUI.Foreground1Color)
                        .clickable {
                            onPostClick()
                        }

                    AsyncImageView(
                        imageUrl = postImage.postImageUrl,
                        modifier = imageModifier
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Post Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPostClick() },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.postLikeFlag) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.postLikeFlag) CatppuccinUI.CurrentPalette.Red else CatppuccinUI.TextColorLight,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .size(20.dp)
                                .clickable { onLikeClick() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (post.postLikesCount > 0) {
                                Text(
                                    text = post.postLikesCount.toSuffixString(),
                                    color = CatppuccinUI.TextColorLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = CatppuccinUI.TextColorLight,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .size(20.dp)
                                .clickable { onCommentClick() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier.width(28.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (post.postCommentsCount > 0) {
                                Text(
                                    text = "${post.postCommentsCount}",
                                    color = CatppuccinUI.TextColorLight.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

//				// Bookmark Button (hidden for own posts)
//				val currentUsername = TokenUtilsProvider.get(LocalContext.current).getUsername()
//				val isOwnPost = currentUsername != null && currentUsername.equals(post.member.memberUsername, ignoreCase = true)
//				if (!isOwnPost) {
//					Icon(
//						imageVector = if (post.postBookmarkFlag) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
//						contentDescription = "Bookmark",
//						tint = CatppuccinUI.TextColorLight,
//						modifier = Modifier
//                            .size(20.dp)
//                            .clickable { onBookmarkClick() }
//					)
//				}
            }
        }
    }
}

private fun formatTimeAgo(dateTime: LocalDateTime): String {
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

