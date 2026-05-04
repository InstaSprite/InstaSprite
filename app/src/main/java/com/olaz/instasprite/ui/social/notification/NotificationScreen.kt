package com.olaz.instasprite.ui.social.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.social.notification.composable.NotificationItem
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.UiUtils

data class Notification(
    val id: String? = null,
    val author: String? = null,
    val body: String? = null,
    val timestamp: Long? = null,
    val read: Boolean? = false
)

// Dummy data
private fun getDummyNotifications(): List<Notification> {
    val now = System.currentTimeMillis()
    return listOf(
        Notification(
            id = "1",
            author = "Sarah Chen",
            body = "liked your photo",
            timestamp = now - 1000 * 60 * 5, // 5 minutes ago
            read = false
        ),
        Notification(
            id = "2",
            author = "Mike Johnson",
            body = "started following you",
            timestamp = now - 1000 * 60 * 30, // 30 minutes ago
            read = false
        ),
        Notification(
            id = "3",
            author = "Emma Rodriguez",
            body = "commented: \"Amazing shot! 🔥\"",
            timestamp = now - 1000 * 60 * 60 * 2, // 2 hours ago
            read = true
        ),
        Notification(
            id = "4",
            author = "Alex Kim",
            body = "liked your photo",
            timestamp = now - 1000 * 60 * 60 * 5, // 5 hours ago
            read = true
        ),
        Notification(
            id = "5",
            author = "Jessica Williams",
            body = "mentioned you in a comment",
            timestamp = now - 1000 * 60 * 60 * 24, // 1 day ago
            read = true
        ),
        Notification(
            id = "6",
            author = "David Park",
            body = "shared your post to their story",
            timestamp = now - 1000 * 60 * 60 * 24 * 2, // 2 days ago
            read = true
        ),
        Notification(
            id = "7",
            author = "Lisa Thompson",
            body = "liked your comment",
            timestamp = now - 1000 * 60 * 60 * 24 * 3, // 3 days ago
            read = true
        ),
        Notification(
            id = "8",
            author = "James Lee",
            body = "started following you",
            timestamp = now - 1000 * 60 * 60 * 24 * 5, // 5 days ago
            read = true
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {}
) {
    val notificationItems by remember { mutableStateOf(getDummyNotifications()) }

    UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CatppuccinUI.TextColorLight
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.notifications),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CatppuccinUI.TextColorLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CatppuccinUI.TopBarColor,
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = CatppuccinUI.BackgroundColor
                )
        ) {
            items(notificationItems.size) { index ->
                NotificationItem(notificationItems[index])
                if (index < notificationItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}


@Preview()
@Composable
fun NotificationScreenPreview() {
    InstaSpriteTheme {
        NotificationScreen()
    }
}
