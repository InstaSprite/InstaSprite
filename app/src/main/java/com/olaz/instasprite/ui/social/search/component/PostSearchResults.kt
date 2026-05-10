package com.olaz.instasprite.ui.social.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.ui.theme.AppTheme
import java.time.LocalDateTime

@Composable
fun PostSearchResults(
    posts: List<PostData>,
    onOpenComments: (Long) -> Unit
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No posts found",
                color = AppTheme.colors.Subtext0Color,
                fontSize = 14.sp
            )
        }
        return
    }

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

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun PostSearchResultsPreview() {
    val samplePosts = listOf(
        PostData(
            postId = 1,
            postContent = "Pixel art result",
            postUploadDate = LocalDateTime.now(),
            member = MemberData(1, "artist1", "Artist", null),
            postCommentsCount = 5,
            postLikesCount = 42,
            postBookmarkFlag = false,
            postLikeFlag = true,
            commentOptionFlag = true,
            isFollowing = false
        )
    )
    PostSearchResults(posts = samplePosts, onOpenComments = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun PostSearchResultsEmptyPreview() {
    PostSearchResults(posts = emptyList(), onOpenComments = {})
}
