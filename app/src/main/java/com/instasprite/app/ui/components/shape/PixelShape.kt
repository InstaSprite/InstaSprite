package com.instasprite.app.ui.components.shape

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.Constants.PIXEL_DP

class PixelShape(
    private val steps: Int = 1
) : CornerBasedShape(
    topStart = CornerSize((PIXEL_DP * 2).dp),
    topEnd = CornerSize((PIXEL_DP * 2).dp),
    bottomEnd = CornerSize((PIXEL_DP * 2).dp),
    bottomStart = CornerSize((PIXEL_DP * 2).dp)
) {
    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val p = topStart
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, steps * p)

            // top left
            for (i in 0 until steps) {
                lineTo(i * p, (steps - i) * p)
                lineTo((i + 1) * p, (steps - i) * p)
            }
            lineTo(steps * p, 0f)

            // top right
            lineTo(w - steps * p, 0f)
            for (i in 0 until steps) {
                lineTo(w - (steps - i) * p, i * p)
                lineTo(w - (steps - i) * p, (i + 1) * p)
            }
            lineTo(w, steps * p)

            // bottom right
            lineTo(w, h - steps * p)
            for (i in 0 until steps) {
                lineTo(w - i * p, h - (steps - i) * p)
                lineTo(w - (i + 1) * p, h - (steps - i) * p)
            }
            lineTo(w - steps * p, h)

            // bottom left
            lineTo(steps * p, h)
            for (i in 0 until steps) {
                lineTo((steps - i) * p, h - i * p)
                lineTo((steps - i) * p, h - (i + 1) * p)
            }
            lineTo(0f, h - steps * p)

            close()
        }
        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): CornerBasedShape = PixelShape(steps)
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .clip(PixelShape(3))
                    .background(AppTheme.colors.LinkColor)
            )
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .border(width = 1.5.dp, AppTheme.colors.LinkColor, PixelShape(2))
                    .clip(PixelShape(2))
                    .background(AppTheme.colors.BackgroundColor)
            )
        }
    }
}