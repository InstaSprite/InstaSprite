package com.instasprite.app.ui.drawing.timeline

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.instasprite.app.utils.pixelDp

internal fun Modifier.timelineGridBorder(
    color: Color,
    topColor: Color = color,
    top: Boolean = false,
    left: Boolean = false
): Modifier =
    drawWithContent {
        drawContent()
        val width = 1.pixelDp.toPx()
        if (top) drawLine(topColor, Offset(0f, width / 2), Offset(size.width, width / 2), width)
        if (left) drawLine(color, Offset(width / 2, 0f), Offset(width / 2, size.height), width)
        drawLine(
            color, Offset(size.width - width / 2, 0f),
            Offset(size.width - width / 2, size.height), width
        )
        drawLine(
            color, Offset(0f, size.height - width / 2),
            Offset(size.width, size.height - width / 2), width
        )
    }
