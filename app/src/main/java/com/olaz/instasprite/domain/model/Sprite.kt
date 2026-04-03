package com.olaz.instasprite.domain.model

data class Sprite(
    val id: String = "",
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val colorPalette: List<Int>? = null
) {
    val compositedPixels: IntArray get() {
        val result = IntArray(width * height)
        for (layer in layers) {
            if (layer.isVisible) {
                for (i in layer.cel.pixels.indices) {
                    if (layer.cel.pixels[i] != 0) {
                        result[i] = layer.cel.pixels[i]
                    }
                }
            }
        }
        return result
    }
}
