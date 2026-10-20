package com.instasprite.app.domain.model

import com.instasprite.app.utils.blendPixel
import com.instasprite.app.utils.TILE_SIZE

data class Sprite(
    val id: String = "",
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val colorPalette: List<Int>? = null,
    val animation: Animation? = null
) {
    fun layersAt(frameId: String): List<Layer> = animation?.let { document ->
        layers.map { Layer(it.id, it.name, it.isVisible, it.isLocked, it.opacity, it.blendMode, document.tiles(it.id, frameId)) }
    } ?: layers

    fun renderFrame(
        frameId: String,
        pixels: IntArray = IntArray(Math.multiplyExact(width, height))
    ): IntArray {
        require(width >= 0 && height >= 0 && pixels.size.toLong() == width.toLong() * height)
        pixels.fill(0)
        for (layer in layersAt(frameId)) {
            if (!layer.isVisible || layer.opacity <= 0f) continue
            for ((coord, tile) in layer.tiles) {
                val originX = coord.x.toLong() * TILE_SIZE
                val originY = coord.y.toLong() * TILE_SIZE
                if (originX >= width || originY >= height || originX + TILE_SIZE <= 0 || originY + TILE_SIZE <= 0) continue
                val left = maxOf(0L, -originX).toInt()
                val top = maxOf(0L, -originY).toInt()
                val right = minOf(TILE_SIZE.toLong(), width - originX).toInt()
                val bottom = minOf(TILE_SIZE.toLong(), height - originY).toInt()
                for (row in top until bottom) {
                    val destination = (originY.toInt() + row) * width + originX.toInt()
                    val source = row * TILE_SIZE
                    for (col in left until right) {
                        val index = destination + col
                        pixels[index] = blendPixel(pixels[index], tile[source + col], layer.opacity, layer.blendMode)
                    }
                }
            }
        }
        return pixels
    }

    val compositedPixels: IntArray
        get() = renderFrame(animation?.posterFrameId.orEmpty())

    override fun toString(): String {
        return "Sprite(id='$id', width=$width, height=$height, layers=${layers.size}, colorPaletteSize=${colorPalette?.size ?: 0})"
    }
}
