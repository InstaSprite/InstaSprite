package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme.colors
import com.instasprite.app.utils.noRippleClickable
import com.instasprite.app.utils.pixelDp

@Composable
fun PixelPerfectOption(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(24.pixelDp)
            .clip(MaterialTheme.shapes.small)
            .background(colors.BackgroundColorDarker)
            .noRippleClickable(onToggle)
    ) {
        Spacer(modifier = Modifier.width(8.pixelDp))
        Text(
            text = stringResource(R.string.pixel_perfect),
            color = colors.TextColorLight,
            fontSize = 12.sp,
        )
        Checkbox(
            checked = isActive,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.LinkColor,
                uncheckedColor = colors.Subtext0Color,
                checkmarkColor = colors.TextColorDark
            )
        )
    }
}
