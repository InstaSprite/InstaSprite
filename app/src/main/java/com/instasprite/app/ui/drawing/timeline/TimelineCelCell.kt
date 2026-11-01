package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
internal fun TimelineCelCell(
    hasCel: Boolean,
    isLinked: Boolean,
    linkLeft: Boolean,
    linkRight: Boolean,
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val background = if (isSelected) colors.purple else colors.surfaceInset
    val ink = if (isSelected) colors.onAccent else colors.onSurface

    Box(
        modifier
            .background(background)
            .timelineGridBorder(colors.surface)
            .semantics { contentDescription = description; selected = isSelected }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Canvas(Modifier.matchParentSize()) {
            val line = 1.pixelDp.toPx()
            val radius = 1.pixelDp.toPx()
            if (linkLeft) drawLine(
                ink,
                Offset(0f, center.y),
                Offset(center.x - radius, center.y),
                line * 2
            )
            if (linkRight) drawLine(
                ink,
                Offset(center.x + radius, center.y),
                Offset(size.width, center.y),
                line * 2
            )
        }
        if (hasCel) PixelIcon(
            icon = R.drawable.ic_small_circle_shape,
            modifier = Modifier.align(Alignment.Center),
            tint = ink
        )
    }
}

@Preview(name = "Cel states", showBackground = true)
@Composable
private fun TimelineCelCellPreview() {
    InstaSpriteTheme {
        Surface {
            Row {
                repeat(5) { index ->
                    TimelineCelCell(
                        index != 0, index in 2..3, index == 3, index == 2,
                        index == 4, "Cel ${index + 1}", {}, Modifier.size(22.pixelDp, 16.pixelDp)
                    )
                }
            }
        }
    }
}
