package com.olaz.instasprite.ui.social.search.component

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.theme.AppTheme
import java.time.LocalDateTime

@Composable
fun TrendingSection(
    isLoading: Boolean,
    posts: List<PostData>,
    onOpenComments: (Long) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.trending),
            color = AppTheme.colors.TextColorLight,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_trending_posts_yet),
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(posts, key = { it.postId }) { post ->
                    TrendingPostThumbnail(
                        post = post,
                        onClick = { onOpenComments(post.postId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingPostThumbnail(
    post: PostData,
    onClick: () -> Unit
) {
    val imageUrl = post.postImages.firstOrNull()?.postImageUrl

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(AppTheme.colors.Foreground0Color)
            .clickable(onClick = onClick)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.postContent.take(40),
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 10.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(AppTheme.colors.BackgroundColorDarker)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = AppTheme.colors.DismissButtonColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${post.postLikesCount}",
                color = AppTheme.colors.TextColorLight.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun TrendingSectionPreview() {
    val samplePosts = listOf(
        PostData(
            postId = 1,
            postContent = "My pixel art character #pixelart",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(1, "artist1", "Pixel Artist", null),
            postCommentsCount = 5,
            postLikesCount = 42,
            postBookmarkFlag = false,
            postLikeFlag = true,
            commentOptionFlag = true,
            isFollowing = false
        ),
        PostData(
            postId = 2,
            postContent = "Game dev progress #gamedev",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(2, "dev99", "Game Dev", null),
            postCommentsCount = 12,
            postLikesCount = 128,
            postBookmarkFlag = false,
            postLikeFlag = false,
            commentOptionFlag = true,
            isFollowing = true
        )
    )
    TrendingSection(isLoading = false, posts = samplePosts, onOpenComments = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun TrendingSectionEmptyPreview() {
    TrendingSection(isLoading = false, posts = emptyList(), onOpenComments = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun TrendingSectionLoadingPreview() {
    TrendingSection(isLoading = true, posts = emptyList(), onOpenComments = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun TrendingPostThumbnailPreview() {
    TrendingPostThumbnail(
        post = PostData(
            postId = 1,
            postContent = "Sample pixel art post content",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(1, "artist1", "Artist", null),
            postCommentsCount = 3,
            postLikesCount = 99,
            postBookmarkFlag = false,
            postLikeFlag = false,
            commentOptionFlag = true,
            isFollowing = false
        ),
        onClick = {}
    )
}
