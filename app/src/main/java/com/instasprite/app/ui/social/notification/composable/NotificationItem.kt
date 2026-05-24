package com.instasprite.app.ui.social.notification.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.domain.model.GroupedNotificationData
import com.instasprite.app.domain.model.NotificationType
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.TimeUtils
import com.instasprite.app.utils.pixelDp

@Composable
fun NotificationItem(notification: GroupedNotificationData, onClick: () -> Unit = {}) {
    val context = LocalContext.current

    val iconRes = when (notification.type) {
        NotificationType.LIKE -> R.drawable.ic_heart
        NotificationType.COMMENT -> R.drawable.ic_comment
        NotificationType.FOLLOW -> R.drawable.ic_profile
        else -> null
    }

    val iconColor = when (notification.type) {
        NotificationType.LIKE -> AppTheme.colors.DismissButtonColor
        NotificationType.FOLLOW -> AppTheme.colors.LinkColor
        NotificationType.COMMENT -> AppTheme.colors.AccentButtonColor
        else -> AppTheme.colors.Foreground2Color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead)
                    AppTheme.colors.Foreground0Color
                else
                    AppTheme.colors.BackgroundColor
            )
            .clickable { onClick() }
            .padding(horizontal = 10.pixelDp, vertical = 8.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(44.pixelDp)) {
            ProfileImage(
                imageUrl = notification.recentActors.firstOrNull()?.avatarUrl,
                size = 40.pixelDp,
                modifier = Modifier.align(Alignment.Center)
            )

            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            AppTheme.colors.BackgroundColor, shape = PixelShape()
                        )
                ) {
                    PixelIcon(
                        icon = iconRes,
                        tint = iconColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.pixelDp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.buildDisplayText(context),
                fontSize = 14.sp,
                color = AppTheme.colors.TextColorLight,
                lineHeight = 20.sp,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.pixelDp))

            Text(
                text = TimeUtils.formatTimeAgo(context, notification.updatedAt),
                fontSize = 12.sp,
                color = AppTheme.colors.TextColorLight
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(6.pixelDp)
                    .background(AppTheme.colors.LinkColor)
            )
        }
    }
}