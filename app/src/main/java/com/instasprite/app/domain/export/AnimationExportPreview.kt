package com.instasprite.app.domain.export

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.LayerCompositor
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE

data class ExportPreviewPixels(
    val width: Int,
    val height: Int,
    val exportWidth: Int,
    val exportHeight: Int,
    val pixels: IntArray
)

object AnimationExportPreview {
    fun renderFrame(
        sprite: Sprite,
        frameId: String,
        checkCancelled: () -> Unit = {}
    ): ExportPreviewPixels {
        val animation = requireNotNull(sprite.animation)
        val frame = animation.frames.first { it.id == frameId }
        val singleFrame = sprite.copy(animation = animation.copy(
            frames = listOf(frame),
            cels = animation.cels.filter { it.frameId == frameId },
            posterFrameId = frameId
        ))
        return render(singleFrame, AnimationExportFormat.SPRITE_SHEET, 100, 1, frameId, checkCancelled = checkCancelled)
    }

    fun render(
        sprite: Sprite,
        format: AnimationExportFormat,
        scalePercent: Int,
        columns: Int,
        frameId: String,
        gifQuantized: Boolean? = null,
        checkCancelled: () -> Unit = {}
    ): ExportPreviewPixels {
        val (cellWidth, cellHeight) = ExportScale.size(sprite.width, sprite.height, scalePercent)
        val sheet = format == AnimationExportFormat.SPRITE_SHEET
        val layout = if (sheet) SpriteSheetExporter.layout(sprite, columns, scalePercent) else null
        val exportWidth = layout?.width ?: cellWidth
        val exportHeight = layout?.height ?: cellHeight
        if (!sheet) require(exportWidth <= 65535 && exportHeight <= 65535)
        val edge = maxOf(exportWidth, exportHeight).coerceAtMost(256)
        val width = (exportWidth.toLong() * edge / maxOf(exportWidth, exportHeight)).toInt()
            .coerceAtLeast(1)
        val height = (exportHeight.toLong() * edge / maxOf(exportWidth, exportHeight)).toInt()
            .coerceAtLeast(1)
        val pixels = IntArray(width * height)
        val frames = sprite.animation?.frames.orEmpty()
        val layersByFrame = mutableMapOf<String, List<Layer>>()
        for (row in 0 until height) {
            checkCancelled()
            val y = ExportScale.sourceIndex(row, exportHeight, height)
            val sourceY = ExportScale.sourceIndex(y % cellHeight, sprite.height, cellHeight)
            for (col in 0 until width) {
                val x = ExportScale.sourceIndex(col, exportWidth, width)
                val index = if (sheet) y / cellHeight * columns + x / cellWidth else 0
                if (sheet && index >= (frames.size.takeIf { it > 0 } ?: 1)) continue
                val id = if (sheet) frames.getOrNull(index)?.id.orEmpty() else frameId
                val layers = layersByFrame.getOrPut(id) { sprite.layersAt(id) }
                val sourceX = ExportScale.sourceIndex(x % cellWidth, sprite.width, cellWidth)
                val tile = TileCoord(sourceX / TILE_SIZE, sourceY / TILE_SIZE)
                val offset = sourceY % TILE_SIZE * TILE_SIZE + sourceX % TILE_SIZE
                val pixel = LayerCompositor.pixel(layers) { it.tiles[tile]?.get(offset) ?: 0 }
                pixels[row * width + col] =
                    if (sheet) pixel else if (pixel ushr 24 < 128) 0 else pixel or (0xff shl 24)
            }
        }
        if (!sheet) GifExporter.applyPreviewPalette(
            pixels,
            gifQuantized ?: GifExporter.requiresQuantization(sprite, frameId, scalePercent, checkCancelled)
        )
        return ExportPreviewPixels(width, height, exportWidth, exportHeight, pixels)
    }
}
