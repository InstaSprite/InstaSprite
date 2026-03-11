package com.olaz.instasprite.domain.export

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.olaz.instasprite.domain.model.Sprite
import java.io.File
import java.io.FileOutputStream

object ImageExporter {
    fun convertToBitmap(
        pixelsData: IntArray,
        canvasWidth: Int,
        canvasHeight: Int,
        scalePercent: Int = 100
    ): Bitmap? {
        if (pixelsData.isEmpty()) return null

        val bitmap = createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixelsData, 0, canvasWidth, 0, 0, canvasWidth, canvasHeight)

        val scale = scalePercent / 100f
        val scaledWidth = (canvasWidth * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (canvasHeight * scale).toInt().coerceAtLeast(1)

        return bitmap.scale(scaledWidth, scaledHeight, false)
    }

    fun saveThumbnail(sprite: Sprite, context: Context) {
        try {
            val bitmap = convertToBitmap(
                sprite.compositedPixels,
                sprite.width,
                sprite.height
            )
            val thumbnailFile = File(context.filesDir, "thumbnail_${sprite.id}.png")
            val outputStream = FileOutputStream(thumbnailFile)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("ImageExporter", "Failed to save thumbnail", e)
        }
    }
}
