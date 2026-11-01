package com.instasprite.app.ui.setting.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun SettingItem(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelIcon(
            icon = icon,
            contentDescription = title,
            tint = AppTheme.colors.onSurface,
        )

        Spacer(modifier = Modifier.width(10.pixelDp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.pixelDp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = AppTheme.colors.textMuted
            )
        }

        trailing()
    }
}
