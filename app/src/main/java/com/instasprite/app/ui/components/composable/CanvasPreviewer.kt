package com.instasprite.app.ui.components.composable

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun CanvasPreviewer(
    sprite: Sprite,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
    onClick: () -> Unit = {}
) {
    if (sprite.width == 0 || sprite.height == 0) return

    val spriteAspectRatio = sprite.width.toFloat() / sprite.height.toFloat()

    val bitmapImage by remember(sprite) {
        mutableStateOf(
            ImageExporter.convertToBitmap(
                sprite.compositedPixels,
                sprite.width,
                sprite.height,
            )?.asImageBitmap()
        )
    }
    if (bitmapImage == null) return

    var modifier = modifier

    if (showBorder) {
        modifier = modifier.border(5.dp, AppTheme.colors.BackgroundColorDarker)
    }

    Image(
        bitmap = bitmapImage!!,
        contentDescription = stringResource(R.string.sprite_preview),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .aspectRatio(spriteAspectRatio)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
    )
}