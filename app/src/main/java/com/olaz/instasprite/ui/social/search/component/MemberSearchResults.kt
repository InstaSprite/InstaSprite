package com.olaz.instasprite.ui.social.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun MemberSearchResults(
    members: List<MemberData>,
    onOpenProfile: (String) -> Unit
) {
    if (members.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users found",
                color = CatppuccinUI.Subtext0Color,
                fontSize = 14.sp
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(members, key = { it.memberId }) { member ->
            MemberSearchItem(
                member = member,
                onClick = { onOpenProfile(member.memberUsername) }
            )
        }
    }
}

@Composable
fun MemberSearchItem(
    member: MemberData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CatppuccinUI.BackgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val avatarUrl = member.memberImage?.imageUrl

        if (avatarUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CatppuccinUI.Foreground1Color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.memberName.take(1).uppercase(),
                    color = CatppuccinUI.TextColorLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = member.memberUsername,
                color = CatppuccinUI.TextColorLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = member.memberName,
                color = CatppuccinUI.Subtext0Color,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun MemberSearchResultsPreview() {
    val members = listOf(
        MemberData(1, "pixelking", "Pixel King", null),
        MemberData(2, "artmaster", "Art Master", null),
        MemberData(3, "spritedev", "Sprite Developer", null)
    )
    MemberSearchResults(members = members, onOpenProfile = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun MemberSearchResultsEmptyPreview() {
    MemberSearchResults(members = emptyList(), onOpenProfile = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun MemberSearchItemPreview() {
    MemberSearchItem(
        member = MemberData(1, "pixelking", "Pixel King", null),
        onClick = {}
    )
}
