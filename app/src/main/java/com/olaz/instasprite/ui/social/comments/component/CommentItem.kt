package com.olaz.instasprite.ui.social.comments.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.olaz.instasprite.ui.social.comments.contract.Comment
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.formatTimeAgo

@Composable
fun CommentItem(
    comment: Comment,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onReplyClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp + if (comment.parentId != null) 24.dp else 0.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            )
    ) {
        if (!comment.profileImageUrl.isNullOrBlank()) {
            AsyncImageView(
                imageUrl = comment.profileImageUrl,
                altText = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick(comment.username) },
            )
        } else {
            Image(
                painter = painterResource(id = comment.profileImageRes),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick(comment.username) },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    color = CatppuccinUI.TextColorLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onProfileClick(comment.username) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    color = CatppuccinUI.TextColorLight.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                color = CatppuccinUI.TextColorLight,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.like_comment),
                    tint = if (comment.isLiked) CatppuccinUI.DismissButtonColor else CatppuccinUI.TextColorLight,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onLikeClick(comment.id) }
                )

                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.foundation.layout.Box(modifier = Modifier.width(24.dp)) {
                    if (comment.likesCount > 0) {
                        Text(
                            text = "${comment.likesCount}",
                            color = CatppuccinUI.TextColorLight.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reply),
                    color = CatppuccinUI.TextColorLight.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onReplyClick(comment.id) }
                )

            }
        }

        if (comment.isOwnComment) {
            IconButton(onClick = { onDeleteClick(comment.id) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete_comment),
                    tint = CatppuccinUI.TextColorLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}


