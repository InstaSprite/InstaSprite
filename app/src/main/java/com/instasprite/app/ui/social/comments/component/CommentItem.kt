package com.instasprite.app.ui.social.comments.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.social.comments.contract.Comment
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.TimeUtils

@Composable
fun CommentItem(
    comment: Comment,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onReplyClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isReply = comment.parentId != null

    val likeScale by animateFloatAsState(
        targetValue = if (comment.isLiked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "like_scale"
    )

    val lineColor = AppTheme.colors.Foreground2Color.copy(alpha = 0.5f)
    val replyBackgroundColor = AppTheme.colors.Foreground0Color.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isReply) {
                    val lineX = 10.pixelDp.toPx() + 10.pixelDp.toPx() // Parent avatar center approx
                    drawLine(
                        color = lineColor,
                        start = Offset(x = lineX, y = 0f),
                        end = Offset(x = lineX, y = size.height),
                        strokeWidth = 1.pixelDp.toPx()
                    )
                    
                    // Small horizontal branch
                    drawLine(
                        color = lineColor,
                        start = Offset(x = lineX, y = size.height / 2f),
                        end = Offset(x = lineX + 10.pixelDp.toPx(), y = size.height / 2f),
                        strokeWidth = 1.pixelDp.toPx()
                    )
                }
            }
            .padding(
                start = 10.pixelDp + if (isReply) 24.pixelDp else 1.pixelDp,
                end = 10.pixelDp,
                top = 6.pixelDp,
                bottom = 6.pixelDp
            )
    ) {
        val avatarModifier = Modifier
            .size(if (isReply) 16.pixelDp else 22.pixelDp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onProfileClick(comment.username) }

        ProfileImage(
            imageUrl = comment.profileImageUrl,
            modifier = avatarModifier,
            size = if (isReply) 16.pixelDp else 22.pixelDp
        )

        Spacer(modifier = Modifier.width(8.pixelDp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(if (isReply) replyBackgroundColor else Color.Transparent)
                .padding(if (isReply) 8.pixelDp else 1.pixelDp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.displayName,
                    color = AppTheme.colors.TextColorLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onProfileClick(comment.username) }
                )
                Spacer(modifier = Modifier.width(6.pixelDp))
                Text(
                    text = TimeUtils.formatTimeAgo(context, comment.createdAt),
                    color = AppTheme.colors.Foreground2Color,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(2.pixelDp))

            Text(
                text = comment.content,
                color = AppTheme.colors.TextColorLight,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.pixelDp)
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onLikeClick(comment.id) }
                        .padding(end = 6.pixelDp, top = 2.pixelDp, bottom = 2.pixelDp)
                ) {
                    PixelIcon(
                        icon = R.drawable.ic_heart,
                        contentDescription = stringResource(R.string.like_comment),
                        tint = if (comment.isLiked) AppTheme.colors.DismissButtonColor else AppTheme.colors.Foreground2Color,
                        modifier = Modifier
                            .scale(likeScale)
                    )
                    Spacer(modifier = Modifier.width(2.pixelDp))
                    if (comment.likesCount > 0) {
                        Text(
                            text = "${comment.likesCount}",
                            color = AppTheme.colors.Foreground2Color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.pixelDp))
                // Reply Button
                if (!isReply) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onReplyClick(comment.id) }
                            .padding(end = 6.pixelDp, top = 2.pixelDp, bottom = 2.pixelDp)
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_reply,
                            contentDescription = stringResource(R.string.reply),
                            tint = AppTheme.colors.TextColorLight,
                        )
                        Spacer(modifier = Modifier.width(2.pixelDp))
                        Text(
                            text = stringResource(R.string.reply),
                            color = AppTheme.colors.TextColorLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

            }
        }

        if (comment.isOwnComment) {
            IconButton(
                onClick = { onDeleteClick(comment.id) },
                modifier = Modifier.size(22.pixelDp)
            ) {
                PixelIcon(
                    icon = R.drawable.ic_trash,
                    contentDescription = stringResource(R.string.delete_comment),
                    tint = AppTheme.colors.Foreground2Color,
                )
            }
        }
    }
}

