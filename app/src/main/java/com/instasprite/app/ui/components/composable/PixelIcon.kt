package com.instasprite.app.ui.components.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.instasprite.app.utils.Constants.PIXEL_DP
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

    val width = (size.width * PIXEL_DP * scale).dp
    val height = (size.height * PIXEL_DP * scale).dp

    Image(
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = modifier.size(width, height)
    )
}