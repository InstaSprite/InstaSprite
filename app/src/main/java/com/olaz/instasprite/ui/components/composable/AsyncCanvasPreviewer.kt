package com.olaz.instasprite.ui.components.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.drawCheckerboard
import java.io.File

@Composable
fun AsyncCanvasPreviewer(
    sprite: Sprite,
    meta: SpriteMeta?,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
    onClick: () -> Unit = {}
) {
    if (sprite.width == 0 || sprite.height == 0) return

    val spriteAspectRatio = sprite.width.toFloat() / sprite.height.toFloat()

    var modifier = modifier

    if (showBorder) {
        modifier = modifier.border(5.dp, CatppuccinUI.BackgroundColorDarker)
    }


    // for previewing in IDE
    if (LocalInspectionMode.current) {
        PreviewImage(
            modifier = modifier,
            sprite = sprite,
            onClick = onClick,
            spriteAspectRatio = spriteAspectRatio
        )
        return
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(LocalContext.current.filesDir, "thumbnail_${sprite.id}.png"))
            .crossfade(true)
            .memoryCacheKey("thumbnail_${sprite.id}_${meta?.lastModifiedAt}")
            .diskCacheKey("thumbnail_${sprite.id}_${meta?.lastModifiedAt}")
            .build(),
        contentDescription = "Sprite Preview",
        modifier = modifier
            .aspectRatio(spriteAspectRatio)
            .drawCheckerboard(sprite.width, sprite.height)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentScale = ContentScale.FillWidth,
        filterQuality = FilterQuality.None
    )
}

@Composable
private fun PreviewImage(
    sprite: Sprite,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    spriteAspectRatio: Float
) {
    val bitmapImage = remember(sprite.compositedPixels, sprite.width, sprite.height) {
        val bitmap = createBitmap(sprite.width, sprite.height, Bitmap.Config.ARGB_8888)
        val argbPixels = sprite.compositedPixels.toIntArray()
        bitmap.setPixels(argbPixels, 0, sprite.width, 0, 0, sprite.width, sprite.height)
        bitmap.asImageBitmap()
    }
    Image(
        bitmap = bitmapImage,
        contentDescription = "Sprite Preview",
        modifier = modifier
            .aspectRatio(spriteAspectRatio)
            .drawCheckerboard(sprite.width, sprite.height)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentScale = ContentScale.FillWidth,
        filterQuality = FilterQuality.None
    )
}