package com.instasprite.app.ui.components.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.instasprite.app.utils.rememberPixelPainter

@Composable
fun PixelIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    scale: Float = 1f,
    tint: Color? = null
) {
    val painter = rememberPixelPainter(icon)

    val size = painter.intrinsicSize

    // convert px -> target dp scale, 16px -> 24dp = 1.5x ratio
    val width = (size.width * 1.5f * scale).dp
    val height = (size.height * 1.5f * scale).dp

    Image(
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = modifier.size(width, height)
    )
}