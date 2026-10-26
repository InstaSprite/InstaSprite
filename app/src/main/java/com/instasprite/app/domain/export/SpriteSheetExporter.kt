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
    fun layout(sprite: Sprite, columns: Int): SpriteSheetLayout {
        val count = sprite.animation?.frames?.size ?: 1
        require(count in 1..Animation.MAX_FRAMES && columns in 1..count)
        require(sprite.width > 0 && sprite.height > 0)
        val width = sprite.width.toLong() * columns
        val height = sprite.height.toLong() * ((count + columns - 1) / columns)
        if (width > Int.MAX_VALUE || height > Int.MAX_VALUE) throw CanvasMemoryException()
        return SpriteSheetLayout(width.toInt(), height.toInt(), columns, sprite.width, sprite.height, count)
    }

    fun render(sprite: Sprite, columns: Int, consume: (SpriteSheetLayout, Int, IntArray) -> Unit) {
        val layout = layout(sprite, columns)
        CanvasMemoryGuard.requireAvailable(
            CanvasMemoryGuard.estimatePixels(layout.width, layout.height, 4),
            CanvasMemoryGuard.estimatePixels(sprite.width, sprite.height, 4)
        )
        val frameIds = sprite.animation?.validated()?.frames?.map { it.id } ?: listOf("")
        val pixels = IntArray(Math.multiplyExact(sprite.width, sprite.height))
        frameIds.forEachIndexed { index, id ->
            sprite.renderFrame(id, pixels)
            consume(layout, index, pixels)
        }
    }
}
