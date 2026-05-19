package com.instasprite.app.ui.components.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instasprite.app.utils.rememberPixelPainter

@Composable
fun PixelIcon(
    @DrawableRes icon: Int,
    contentDescription: String? = null,
    modifier: Modifier
) {
    Image(
        painter = rememberPixelPainter(icon),
        contentDescription = contentDescription,
        modifier = modifier
    )
}