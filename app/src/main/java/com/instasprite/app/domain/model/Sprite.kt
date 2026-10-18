package com.instasprite.app.domain.model

import com.instasprite.app.utils.blendPixel
import com.instasprite.app.utils.inflateCel

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

    fun renderFrame(frameId: String): IntArray = copy(layers = layersAt(frameId), animation = null).compositedPixels

    val compositedPixels: IntArray
        get() {
            animation?.let { return renderFrame(it.posterFrameId) }
            val result = IntArray(width * height)
            for (layer in layers) {
                if (layer.isVisible) {
                    val pixels = inflateCel(layer.cel, width, height)
                    for (i in pixels.indices) {
                        result[i] = blendPixel(result[i], pixels[i], layer.opacity, layer.blendMode)
                    }
                }
            }
            return result
        }

    override fun toString(): String {
        return "Sprite(id='$id', width=$width, height=$height, layers=${layers.size}, colorPaletteSize=${colorPalette?.size ?: 0})"
    }
}
