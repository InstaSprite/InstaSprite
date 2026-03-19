package com.olaz.instasprite.domain.model

data class Cel(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val pixels: IntArray
) {
    init {
        require(width >= 0) { "width must be non-negative" }
        require(height >= 0) { "height must be non-negative" }
        require(pixels.size == width * height) {
            "pixels size must match cel bounds"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cel) return false

        return x == other.x &&
                y == other.y &&
                width == other.width &&
                height == other.height &&
                pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}
