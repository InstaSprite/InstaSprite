package com.instasprite.app.ui.social.comments.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.noRippleClickable

@Composable
fun CommentActions(
    isLiked: Boolean,
    isBookmarked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    showBookmark: Boolean = true
) {
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "likeScale"
    )
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bookmarkScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.noRippleClickable { onLikeClick() }
        ) {
            PixelIcon(
                icon = R.drawable.ic_heart,
                contentDescription = stringResource(R.string.like),
                tint = if (isLiked) AppTheme.colors.DismissButtonColor else AppTheme.colors.TextColorLight,
                modifier = Modifier
                    .scale(likeScale)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier.width(32.dp),
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
            PixelIcon(
                icon = R.drawable.ic_bookmark,
                contentDescription = stringResource(R.string.bookmark),
                tint = if (isBookmarked) AppTheme.colors.LinkColor else AppTheme.colors.TextColorLight,
                modifier = Modifier
                    .scale(bookmarkScale)
                    .noRippleClickable { onBookmarkClick() }
            )
        }
    }
}
