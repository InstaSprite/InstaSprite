package com.instasprite.app.ui.drawing.dialog

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.domain.draw.AnimationPlayback
import com.instasprite.app.domain.export.AnimationExportFormat
import com.instasprite.app.domain.export.AnimationExportPreview
import com.instasprite.app.domain.export.GifExporter
import com.instasprite.app.domain.export.ExportPreviewPixels
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.CanvasMemoryException
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private data class PreviewFrame(
    val image: ImageBitmap,
    val pixels: ExportPreviewPixels,
    val index: Int
)

@Composable
internal fun AnimationExportPreviewView(
    sprite: Sprite,
    format: AnimationExportFormat,
    scale: Int?,
    columns: Int?
) {
    var preview by remember(sprite, format, scale, columns) { mutableStateOf<PreviewFrame?>(null) }
    var failed by remember(sprite, format, scale, columns) { mutableStateOf(false) }
    LaunchedEffect(sprite, format, scale, columns) {
        if (scale == null || columns == null) return@LaunchedEffect
        delay(150)
        try {
            val frames = sprite.animation?.frames.orEmpty().map {
                it.copy(durationMs = ((it.durationMs + 5) / 10).coerceAtLeast(1) * 10)
            }
            val playback = frames.firstOrNull()?.let { AnimationPlayback(frames, it.id) }
            val start = withFrameMillis { it }
            var lastFrame: String? = null
            val quantizedFrames = mutableMapOf<String, Boolean>()
            while (isActive) {
                val id = playback?.frameAt(withFrameMillis { it } - start).orEmpty()
                if (id != lastFrame) {
                    preview = withContext(Dispatchers.Default) {
                        val quantized = if (format == AnimationExportFormat.GIF) {
                            quantizedFrames.getOrPut(id) {
                                GifExporter.requiresQuantization(sprite, id, scale) { ensureActive() }
                            }
                        } else null
                        val pixels = AnimationExportPreview.render(
                            sprite,
                            format,
                            scale,
                            columns,
                            id,
                            quantized
                        ) { ensureActive() }
                        val bitmap = Bitmap.createBitmap(
                            pixels.pixels,
                            pixels.width,
                            pixels.height,
                            Bitmap.Config.ARGB_8888
                        )
                        PreviewFrame(
                            bitmap.asImageBitmap(),
                            pixels,
                            frames.indexOfFirst { it.id == id }.coerceAtLeast(0)
                        )
                    }
                    lastFrame = id
                }
                if (format == AnimationExportFormat.SPRITE_SHEET || frames.size < 2) break
            }
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: CanvasMemoryException) {
            failed = true
        }
    }
    Column {
        Text(
            stringResource(R.string.animation_export_preview),
            color = AppTheme.colors.TextColorLight
        )
        val image = preview
        if (scale == null || columns == null || failed) {
            Text(
                stringResource(R.string.export_preview_unavailable),
                color = AppTheme.colors.Subtext0Color
            )
        } else if (image == null) {
            CircularProgressIndicator()
        } else {
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val ratio = image.pixels.exportWidth.toFloat() / image.pixels.exportHeight
                Image(
                    image.image,
                    contentDescription = stringResource(R.string.animation_export_preview),
                    modifier = Modifier
                        .width(minOf(maxWidth, 96.pixelDp * ratio))
                        .aspectRatio(ratio)
                        .drawCheckerboard(),
                    contentScale = ContentScale.FillBounds,
                    filterQuality = FilterQuality.None
                )
            }
            Text(
                stringResource(
                    R.string.export_preview_size,
                    image.pixels.exportWidth,
                    image.pixels.exportHeight
                ),
                color = AppTheme.colors.TextColorLight
            )
            if (format == AnimationExportFormat.GIF) {
                Text(
                    stringResource(
                        R.string.export_preview_frame,
                        image.index + 1,
                        sprite.animation?.frames?.size ?: 1
                    ),
                    color = AppTheme.colors.Subtext0Color
                )
            }
        }
    }
}
