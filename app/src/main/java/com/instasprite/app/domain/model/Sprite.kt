package com.instasprite.app.domain.model

import com.instasprite.app.utils.blendPixel
import com.instasprite.app.utils.inflateCel

data class Sprite(
    val id: String = "",
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val colorPalette: List<Int>? = null
) {
    val compositedPixels: IntArray
        get() {
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
}
