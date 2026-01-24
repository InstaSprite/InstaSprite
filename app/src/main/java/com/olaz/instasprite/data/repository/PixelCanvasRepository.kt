package com.olaz.instasprite.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.data.model.PixelCanvasModel

class PixelCanvasRepository(var model: PixelCanvasModel) {
    var width: Int
        get() = model.width
        set(value) { model.width = value }

    var height: Int
        get() = model.height
        set(value) { model.height = value }

    private var pixels = MutableList(width * height){ Color.Transparent }

    fun setPixel(row: Int, col: Int, color: Color) {
        if (row in 0 until height && col in 0 until width) {
            pixels[row * width + col] = color
        }
    }

    fun setPixel(row: Int, col: Int, color: Color, scale: Int) {
        var xStart = row
        var xEnd = row
        var yStart = col
        var yEnd = col

        for (s in 2..scale) {
            if (s % 2 == 0) {
                // Even scale → expand top-left
                xStart -= 1
                yStart -= 1
            } else {
                // Odd scale → expand bottom-right
                xEnd += 1
                yEnd += 1
            }
        }

        // Clamp bounds to stay within matrix limits
        xStart = xStart.coerceAtLeast(0)
        yStart = yStart.coerceAtLeast(0)
        xEnd = xEnd.coerceAtMost(height - 1)
        yEnd = yEnd.coerceAtMost(width - 1)

        // Fill all pixels in the region
        for (row in xStart..xEnd) {
            for (col in yStart..yEnd) {
                setPixel(row, col, color)
            }
        }
    }

    fun getPixel(row: Int, col: Int): Color {
        return if (row in 0 until height && col in 0 until width) {
            pixels[row * width + col]
        } else {
            Color.Transparent
        }
    }

    fun getAllPixels(): List<Color> {
        return pixels.toList()
    }

    fun setAllPixels(colors: List<Color>) {
        if (colors.size == pixels.size) {
            colors.forEachIndexed { index, color ->
                pixels[index] = color
            }
        }
    }

    fun setCanvas(width: Int, height: Int, pixelsData: List<Color>? = null) {
        this.width = width
        this.height = height
        this.pixels = if (pixelsData != null && pixelsData.size == width * height) {
            pixelsData.toMutableList()
        } else {
            MutableList(width * height) { Color.Transparent }
        }
    }

    fun getSprite(): com.olaz.instasprite.domain.model.Sprite {
        return com.olaz.instasprite.domain.model.Sprite(
            width = width,
            height = height,
            pixelsData = pixels.map { it.toArgb() }
        )
    }
}