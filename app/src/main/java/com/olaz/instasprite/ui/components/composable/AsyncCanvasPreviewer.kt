package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta
import com.olaz.instasprite.ui.theme.CatppuccinUI
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