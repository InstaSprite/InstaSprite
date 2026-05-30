package com.instasprite.app.ui.social.feed.component

import com.instasprite.app.utils.pixelDp

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.domain.model.PostData
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.components.composable.ParsedPostText
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.TimeUtils
import com.instasprite.app.utils.noRippleClickable
import com.instasprite.app.utils.toSuffixString

@OptIn(ExperimentalLayoutApi::class)
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
    showBookmarkButton: Boolean = !showDeleteButton,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.BackgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(10.pixelDp)
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
                    ProfileImage(
                        imageUrl = post.member.memberImage?.imageUrl,
                        size = 26.pixelDp
                    )

                    Spacer(modifier = Modifier.width(8.pixelDp))

                    Column {
                        Text(
                            text = post.member.memberName,
                            color = AppTheme.colors.TextColorLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = TimeUtils.formatTimeAgo(context, post.postUploadDate),
                            color = AppTheme.colors.Subtext0Color,
                            fontSize = 12.sp
                        )
                    }
                }

                if (showFollowButton) {
                    IconButton(
                        onClick = {
                            if (post.isFollowing) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.unfollow) + " " + post.member.memberName,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.follow) + " " + post.member.memberName,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            onFollowClick(post.member.memberUsername, post.isFollowing)
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.size(14.pixelDp)
                    ) {
                        PixelIcon(
                            icon =  if (post.isFollowing)
                                R.drawable.ic_profile
                            else
                                R.drawable.ic_follow,
                            contentDescription = stringResource(R.string.follow),
                            tint = if (post.isFollowing)
                                AppTheme.colors.AccentButtonColor
                            else
                                AppTheme.colors.Foreground2Color,
                        )
                    }
                }

                if (showDeleteButton) {
                    IconButton(
                        onClick = { onDeleteClick(post.postId) },
                        modifier = Modifier.size(14.pixelDp)
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_trash,
                            contentDescription = stringResource(R.string.delete),
                            tint = AppTheme.colors.DismissButtonColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            if (post.postContent.isNotEmpty()) {
                ParsedPostText(
                    text = post.postContent,
                    textColor = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.bodyMedium,
                    onHashtagClick = { hashtag -> onHashtagClick(hashtag) },
                    onMentionClick = { mention -> onMentionClick(mention) },
                    onTextClick = { onPostClick() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.pixelDp))
            }

            if (post.hashtags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
                    verticalArrangement = Arrangement.spacedBy(2.pixelDp)
                ) {
                    post.hashtags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            color = AppTheme.colors.SelectedColor,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onHashtagClick(tag) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.pixelDp))
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
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            onPostClick()
                        }

                    AsyncImageView(
                        imageUrl = postImage.postImageUrl,
                        placeHolder = ColorPainter(Color(postImage.dominantColor)),
                        modifier = imageModifier
                    )

                    Spacer(modifier = Modifier.height(8.pixelDp))
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
                    horizontalArrangement = Arrangement.spacedBy(10.pixelDp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_heart,
                            contentDescription = stringResource(R.string.like),
                            tint = if (post.postLikeFlag)
                                AppTheme.colors.DismissButtonColor
                            else
                                AppTheme.colors.TextColorLight,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .noRippleClickable { onLikeClick() }
                        )
                        Spacer(modifier = Modifier.width(2.pixelDp))
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (post.postLikesCount > 0) {
                                Text(
                                    text = post.postLikesCount.toSuffixString(),
                                    color = AppTheme.colors.TextColorLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_comment,
                            contentDescription = stringResource(R.string.comment),
                            tint = AppTheme.colors.TextColorLight,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .noRippleClickable { onCommentClick() }
                        )
                        Spacer(modifier = Modifier.width(2.pixelDp))
                        Box(
                            modifier = Modifier.width(18.pixelDp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (post.postCommentsCount > 0) {
                                Text(
                                    text = "${post.postCommentsCount}",
                                    color = AppTheme.colors.TextColorLight,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                if (showBookmarkButton) {
                    PixelIcon(
                        icon = R.drawable.ic_bookmark,
                        contentDescription = stringResource(R.string.bookmark),
                        tint = if (post.postBookmarkFlag)
                            AppTheme.colors.LinkColor
                        else
                            AppTheme.colors.TextColorLight,
                        modifier = Modifier
                            .size(14.pixelDp)
                            .noRippleClickable { onBookmarkClick() }
                    )
                }
            }
        }
    }
}

