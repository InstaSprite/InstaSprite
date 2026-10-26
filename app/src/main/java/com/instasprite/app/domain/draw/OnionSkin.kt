package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.LayerCompositor
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.blendPixel

object OnionSkin {
    fun neighbors(sprite: Sprite, frameId: String): List<String> {
        val frames = sprite.animation?.frames ?: return emptyList()
        val index = frames.indexOfFirst { it.id == frameId }
        if (index < 0) return emptyList()
        return listOfNotNull(frames.getOrNull(index - 1)?.id, frames.getOrNull(index + 1)?.id)
    }

    fun renderRows(sprite: Sprite, frameId: String, consume: (Int, IntArray, Int) -> Unit) {
        val frames = sprite.animation?.frames ?: return
        val index = frames.indexOfFirst { it.id == frameId }
        if (index < 0 || sprite.width <= 0 || sprite.height <= 0) return
        val previous = frames.getOrNull(index - 1)?.let { sprite.layersAt(it.id) }.orEmpty()
        val next = frames.getOrNull(index + 1)?.let { sprite.layersAt(it.id) }.orEmpty()
        val blockHeight = minOf(TILE_SIZE, sprite.height)
        var before = IntArray(Math.multiplyExact(sprite.width, blockHeight))
        var after = IntArray(before.size)
        for (row in 0 until sprite.height step blockHeight) {
            val rows = minOf(blockHeight, sprite.height - row)
            if (before.size != sprite.width * rows) {
                before = IntArray(sprite.width * rows)
                after = IntArray(before.size)
            }
            LayerCompositor.render(
                previous,
                sprite.width,
                sprite.height,
                before,
                row,
                0,
                sprite.width,
                rows
            )
            LayerCompositor.render(
                next,
                sprite.width,
                sprite.height,
                after,
                row,
                0,
                sprite.width,
                rows
            )
            for (i in before.indices) {
                before[i] = blendPixel(0, before[i], 0.25f)
                before[i] = blendPixel(before[i], after[i], 0.25f)
            }
            consume(row, before, rows)
        }
    }
}
