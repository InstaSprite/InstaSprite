package com.instasprite.app.domain.export

import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.CanvasMemoryException
import com.instasprite.app.utils.CanvasMemoryGuard

data class SpriteSheetLayout(
    val width: Int, val height: Int, val columns: Int,
    val frameWidth: Int, val frameHeight: Int, val frameCount: Int
) {
    fun position(index: Int): Pair<Int, Int> {
        require(index in 0 until frameCount)
        return index % columns * frameWidth to index / columns * frameHeight
    }
}

object SpriteSheetExporter {
    fun layout(sprite: Sprite, columns: Int, scalePercent: Int = 100): SpriteSheetLayout {
        val count = sprite.animation?.frames?.size ?: 1
        require(count in 1..Animation.MAX_FRAMES && columns in 1..count)
        val (frameWidth, frameHeight) = ExportScale.size(sprite.width, sprite.height, scalePercent)
        val width = frameWidth.toLong() * columns
        val height = frameHeight.toLong() * ((count + columns - 1) / columns)
        if (width > Int.MAX_VALUE || height > Int.MAX_VALUE) throw CanvasMemoryException()
        return SpriteSheetLayout(width.toInt(), height.toInt(), columns, frameWidth, frameHeight, count)
    }

    fun render(sprite: Sprite, columns: Int, scalePercent: Int = 100, consume: (SpriteSheetLayout, Int, IntArray) -> Unit) {
        val layout = layout(sprite, columns, scalePercent)
        val resized = layout.frameWidth != sprite.width || layout.frameHeight != sprite.height
        CanvasMemoryGuard.requireAvailable(
            CanvasMemoryGuard.estimatePixels(layout.width, layout.height, 4),
            CanvasMemoryGuard.estimatePixels(sprite.width, sprite.height, 4),
            if (resized) CanvasMemoryGuard.estimatePixels(layout.frameWidth, layout.frameHeight, 4) else 0
        )
        val frameIds = sprite.animation?.validated()?.frames?.map { it.id } ?: listOf("")
        val source = IntArray(Math.multiplyExact(sprite.width, sprite.height))
        val pixels = if (resized) IntArray(Math.multiplyExact(layout.frameWidth, layout.frameHeight)) else source
        frameIds.forEachIndexed { index, id ->
            sprite.renderFrame(id, source)
            if (resized) ExportScale.copy(source, sprite.width, sprite.height, pixels, layout.frameWidth, layout.frameHeight)
            consume(layout, index, pixels)
        }
    }
}
