package com.olaz.instasprite.ui.social.comments.component

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun CommentActions(
    isLiked: Boolean,
    isBookmarked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    showBookmark: Boolean = true
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.like),
                tint = if (isLiked) AppTheme.colors.DismissButtonColor else AppTheme.colors.TextColorLight,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .size(20.dp)
                    .clickable { onLikeClick() }
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (likesCount > 0) {
                    Text(
                        text = "$likesCount",
                        color = AppTheme.colors.TextColorLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showBookmark) {
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = stringResource(R.string.bookmark),
                    tint = AppTheme.colors.TextColorLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}