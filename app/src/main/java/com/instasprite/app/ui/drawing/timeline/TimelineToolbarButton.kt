package com.instasprite.app.ui.drawing.timeline

import androidx.annotation.DrawableRes
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
internal fun TimelineToolbarButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    active: Boolean = false,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Box(
        Modifier
            .size(22.pixelDp)
            .combinedClickable(
                enabled = enabled, role = Role.Button, onClick = onClick,
                onLongClickLabel = onLongClickLabel, onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(
            icon, contentDescription = description, tint = when {
                !enabled -> colors.Subtext1Color
                active -> colors.AccentButtonColor
                else -> colors.TextColorLight
            }
        )
    }
}

@Preview(name = "Transport buttons", showBackground = true)
@Composable
private fun TimelineToolbarButtonPreview() {
    InstaSpriteTheme {
        Row {
            TimelineToolbarButton(R.drawable.ic_left_arrow, "Previous", {}, enabled = false)
            TimelineToolbarButton(R.drawable.ic_arrow_head_right, "Play", {}, active = true)
        }
    }
}
