package com.instasprite.app.domain.export

import com.instasprite.app.utils.CanvasMemoryGuard
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.instasprite.app.domain.model.Sprite
import java.io.File
import java.io.FileOutputStream

object ImageExporter {
    fun convertToBitmap(
        sprite: Sprite,
        frameId: String = sprite.animation?.posterFrameId.orEmpty(),
        scalePercent: Int = 100
    ): Bitmap? {
        val (scaledWidth, scaledHeight) = CanvasMemoryGuard.requireExport(sprite.width, sprite.height, scalePercent)
        val pixels = sprite.renderFrame(frameId)
        val bitmap = createBitmap(sprite.width, sprite.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, sprite.width, 0, 0, sprite.width, sprite.height)

        return try {
            bitmap.scale(scaledWidth, scaledHeight, false).also {
                if (it !== bitmap) bitmap.recycle()
            }
        } catch (e: Exception) {
            bitmap.recycle()
            throw e
        }
    }

    fun convertToSpriteSheet(sprite: Sprite, columns: Int, checkCancelled: () -> Unit): Bitmap {
        var bitmap: Bitmap? = null
        try {
            SpriteSheetExporter.render(sprite, columns) { layout, index, pixels ->
                checkCancelled()
                val target = bitmap ?: createBitmap(layout.width, layout.height).also { bitmap = it }
                val (x, y) = layout.position(index)
                target.setPixels(pixels, 0, sprite.width, x, y, sprite.width, sprite.height)
            }
            return requireNotNull(bitmap)
        } catch (t: Throwable) {
            bitmap?.recycle()
            throw t
        }
    }

    fun saveThumbnail(sprite: Sprite, context: Context) {
        try {
            val bitmap = convertToBitmap(sprite) ?: return
            val thumbnailFile = File(context.filesDir, "thumbnail_${sprite.id}.png")
            try {
                FileOutputStream(thumbnailFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e("ImageExporter", "Failed to save thumbnail", e)
        }
    }
}
