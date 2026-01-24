package com.olaz.instasprite.domain.usecase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.data.repository.PixelCanvasRepository
import com.olaz.instasprite.data.repository.ColorPaletteRepository

class PixelCanvasUseCase(
    private val pixelCanvasRepository: PixelCanvasRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
) {

    fun getCanvasWidth(): Int {
        return pixelCanvasRepository.width
    }

    fun getCanvasHeight(): Int {
        return pixelCanvasRepository.height
    }

    fun setPixel(row: Int, col: Int, color: Color) {
        pixelCanvasRepository.setPixel(row, col, color)
    }

    fun setPixel(row: Int, col: Int, color: Color, scale: Int) {
        pixelCanvasRepository.setPixel(row, col, color, scale)
    }

    fun getPixel(row: Int, col: Int): Color {
        return pixelCanvasRepository.getPixel(row, col)
    }

    fun setCanvas(width: Int, height: Int, pixels: List<Color>? = null) {
        pixelCanvasRepository.setCanvas(width, height, pixels)
    }

    fun setCanvas(sprite: Sprite) {
        val decodedPixels = sprite.pixelsData.map { Color(it) }
        setCanvas(sprite.width, sprite.height, decodedPixels)
    }

    fun setAllPixels(pixels: List<Color>) {
        pixelCanvasRepository.setAllPixels(pixels)
    }

    fun getAllPixels(): List<Color> {
        return pixelCanvasRepository.getAllPixels()
    }

    fun getSprite(): Sprite {
        val colorPalette = colorPaletteRepository.colors.value.map { it.toArgb() }
        return pixelCanvasRepository.getSprite().copy(colorPalette = colorPalette)
    }

    fun selectColor(color: Color) {
        colorPaletteRepository.setActiveColor(color)
    }

    fun rotateCanvas(pixels: List<Color>) {
        val oldWidth = pixelCanvasRepository.width
        val oldHeight = pixelCanvasRepository.height
        val rotatedPixels = MutableList(pixels.size) { Color.Transparent }

        for (row in 0 until oldHeight) {
            for (col in 0 until oldWidth) {
                val oldIndex = row * oldWidth + col
                val newRow = col
                val newCol = oldHeight - 1 - row
                val newIndex = newRow * oldHeight + newCol
                if (newIndex in rotatedPixels.indices && oldIndex in pixels.indices) {
                    rotatedPixels[newIndex] = pixels[oldIndex]
                }
            }
        }

        return pixelCanvasRepository.setCanvas(oldHeight, oldWidth, rotatedPixels)
    }

    fun hFlipCanvas(pixels: List<Color>) {
        val width = pixelCanvasRepository.width
        val height = pixelCanvasRepository.height
        val flippedPixels = mutableListOf<Color>()

        for (row in 0 until height) {
            for (col in width - 1 downTo 0) {
                flippedPixels.add(pixels[row * width + col])
            }
        }
        return setAllPixels(flippedPixels)
    }

    fun vFlipCanvas(pixels: List<Color>) {
        val width = pixelCanvasRepository.width
        val height = pixelCanvasRepository.height
        val flippedPixels = mutableListOf<Color>()

        for (row in height - 1 downTo 0) {
            for (col in 0 until width) {
                flippedPixels.add(pixels[row * width + col])
            }
        }

        return setAllPixels(flippedPixels)
    }

    fun resizeCanvas(newWidth: Int, newHeight: Int) {
        val oldWidth = pixelCanvasRepository.width
        val oldHeight = pixelCanvasRepository.height
        val oldPixels = pixelCanvasRepository.getAllPixels()

        val newPixels = MutableList(newWidth * newHeight) { Color.Transparent }

        val copyWidth = minOf(oldWidth, newWidth)
        val copyHeight = minOf(oldHeight, newHeight)

        for (row in 0 until copyHeight) {
            for (col in 0 until copyWidth) {
                val oldIndex = row * oldWidth + col
                val newIndex = row * newWidth + col
                newPixels[newIndex] = oldPixels[oldIndex]
            }
        }

        pixelCanvasRepository.setCanvas(newWidth, newHeight, newPixels)
    }
}