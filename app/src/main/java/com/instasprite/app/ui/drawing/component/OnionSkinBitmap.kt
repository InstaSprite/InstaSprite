package com.instasprite.app.ui.drawing.component

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import com.instasprite.app.R
import com.instasprite.app.domain.draw.OnionSkin
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.CanvasMemoryException
import com.instasprite.app.utils.CanvasMemoryGuard
import com.instasprite.app.utils.TILE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

@Composable
internal fun rememberOnionSkinBitmap(sprite: Sprite, frameId: String, enabled: Boolean): ImageBitmap? {
    val context = LocalContext.current
    val state = remember(sprite, frameId, enabled) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(sprite, frameId, enabled) {
        if (enabled && OnionSkin.neighbors(sprite, frameId).isNotEmpty()) {
            try {
                state.value = withContext(Dispatchers.Default) {
                    CanvasMemoryGuard.requireAvailable(
                        CanvasMemoryGuard.estimatePixels(sprite.width, sprite.height, 4),
                        CanvasMemoryGuard.estimatePixels(sprite.width, minOf(TILE_SIZE, sprite.height), 8)
                    )
                    val image = createBitmap(sprite.width, sprite.height)
                    try {
                        OnionSkin.renderRows(sprite, frameId) { row, pixels, rows ->
                            ensureActive()
                            image.setPixels(pixels, 0, sprite.width, 0, row, sprite.width, rows)
                        }
                        image.asImageBitmap()
                    } catch (t: Throwable) {
                        image.recycle()
                        throw t
                    }
                }
            } catch (_: CanvasMemoryException) {
                Toast.makeText(context, R.string.operation_memory_limit, Toast.LENGTH_LONG).show()
            }
        }
    }
    return if (enabled) state.value else null
}
