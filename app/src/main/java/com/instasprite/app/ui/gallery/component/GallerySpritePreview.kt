package com.instasprite.app.ui.gallery.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.domain.draw.AnimationPlayback
import com.instasprite.app.domain.export.AnimationExportPreview
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.ui.components.composable.AsyncCanvasPreviewer
import com.instasprite.app.utils.drawCheckerboard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun GallerySpritePreview(
    sprite: Sprite,
    meta: SpriteMeta?,
    playing: Boolean,
    playbackSprite: Sprite?,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val stop by rememberUpdatedState(onStop)
    DisposableEffect(sprite.id, playing) {
        onDispose { if (playing) stop() }
    }
    var image by remember(playbackSprite) { mutableStateOf<ImageBitmap?>(null) }
    var failed by remember(playbackSprite) { mutableStateOf(false) }
    LaunchedEffect(playbackSprite) {
        val source = playbackSprite ?: return@LaunchedEffect
        val frames = source.animation?.frames.orEmpty()
        if (frames.size < 2) return@LaunchedEffect
        try {
            val playback = AnimationPlayback(frames, frames.first().id)
            val start = withFrameMillis { it }
            var previous: String? = null
            while (isActive) {
                val frameId = playback.frameAt(withFrameMillis { it } - start)
                if (frameId == previous) continue
                image = withContext(Dispatchers.Default) {
                    val pixels = AnimationExportPreview.renderFrame(source, frameId) { ensureActive() }
                    Bitmap.createBitmap(pixels.pixels, pixels.width, pixels.height, Bitmap.Config.ARGB_8888)
                        .asImageBitmap()
                }
                previous = frameId
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            failed = true
        }
    }
    val current = image
    when {
        failed -> Text(stringResource(R.string.error_action_failed), modifier = modifier)
        current == null -> AsyncCanvasPreviewer(sprite, meta, modifier, onClick = onClick)
        else -> Image(
            current,
            contentDescription = stringResource(R.string.sprite_preview),
            modifier = modifier
                .aspectRatio(sprite.width.toFloat() / sprite.height)
                .drawCheckerboard(sprite.width, sprite.height)
                .clickable(onClick = onClick),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None
        )
    }
}
