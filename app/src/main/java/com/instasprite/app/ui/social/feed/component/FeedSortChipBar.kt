package com.instasprite.app.ui.social.feed.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.PostFilter
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun FeedSortChipBar(
    currentFilter: PostFilter,
    isLoggedIn: Boolean,
    onSelectFilter: (PostFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.BackgroundColorDarker)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        FilterChip(
            selected = currentFilter == PostFilter.Recent,
            onClick = { onSelectFilter(PostFilter.Recent) },
            label = {
                Text(
                    text = stringResource(R.string.filter_recent),
                    fontSize = 13.sp,
                    fontWeight = if (currentFilter == PostFilter.Recent) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppTheme.colors.SelectedColor,
                selectedLabelColor = AppTheme.colors.TextColorDark,
                containerColor = AppTheme.colors.BackgroundColor,
                labelColor = AppTheme.colors.TextColorLight
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = currentFilter == PostFilter.Recent,
                borderColor = AppTheme.colors.Subtext0Color.copy(alpha = 0.3f),
                selectedBorderColor = AppTheme.colors.SelectedColor
            )
        )

        FilterChip(
            selected = currentFilter == PostFilter.Follow,
            enabled = isLoggedIn,
            onClick = { onSelectFilter(PostFilter.Follow) },
            label = {
                Text(
                    text = stringResource(R.string.filter_follow),
                    fontSize = 13.sp,
                    fontWeight = if (currentFilter == PostFilter.Follow) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppTheme.colors.SelectedColor,
                selectedLabelColor = AppTheme.colors.TextColorDark,
                containerColor = AppTheme.colors.BackgroundColor,
                labelColor = AppTheme.colors.TextColorLight,
                disabledContainerColor = AppTheme.colors.BackgroundColor,
                disabledLabelColor = AppTheme.colors.Subtext0Color.copy(alpha = 0.4f)
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = isLoggedIn,
                selected = currentFilter == PostFilter.Follow,
                borderColor = AppTheme.colors.Subtext0Color.copy(alpha = 0.15f),
                selectedBorderColor = AppTheme.colors.SelectedColor,
                disabledBorderColor = AppTheme.colors.Subtext0Color.copy(alpha = 0.1f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedSortChipBarRecentPreview() {
    InstaSpriteTheme {
        FeedSortChipBar(
            currentFilter = PostFilter.Recent,
            isLoggedIn = true,
            onSelectFilter = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedSortChipBarFollowPreview() {
    InstaSpriteTheme {
        FeedSortChipBar(
            currentFilter = PostFilter.Follow,
            isLoggedIn = true,
            onSelectFilter = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedSortChipBarLoggedOutPreview() {
    InstaSpriteTheme {
        FeedSortChipBar(
            currentFilter = PostFilter.Recent,
            isLoggedIn = false,
            onSelectFilter = {}
        )
    }
}
