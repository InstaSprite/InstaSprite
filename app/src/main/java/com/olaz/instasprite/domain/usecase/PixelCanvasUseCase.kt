package com.olaz.instasprite.domain.usecase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.data.repository.PixelCanvasRepository
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite

class PixelCanvasUseCase(
    private val pixelCanvasRepository: PixelCanvasRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
) {
    fun getLayers(): List<Layer> = pixelCanvasRepository.layers
    fun getActiveLayerId(): String = pixelCanvasRepository.activeLayerId
    fun addLayer(name: String) = pixelCanvasRepository.addLayer(name)
    fun removeLayer(id: String) = pixelCanvasRepository.removeLayer(id)
    fun setActiveLayer(id: String) = pixelCanvasRepository.setActiveLayer(id)
    fun toggleVisibility(id: String) = pixelCanvasRepository.toggleVisibility(id)
    fun toggleLock(id: String) = pixelCanvasRepository.toggleLock(id)
    fun mergeLayerDown(id: String) = pixelCanvasRepository.mergeLayerDown(id)
    fun reorderLayer(fromIndex: Int, toIndex: Int) = pixelCanvasRepository.reorderLayer(fromIndex, toIndex)

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
        pixelCanvasRepository.setCanvasData(sprite.width, sprite.height, sprite.layers)
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

    fun rotateCanvas() {
        pixelCanvasRepository.rotate()
    }

    fun hFlipCanvas() {
        pixelCanvasRepository.horizontalFlip()
    }

    fun vFlipCanvas() {
        pixelCanvasRepository.verticalFlip()
    }

    fun resizeCanvas(newWidth: Int, newHeight: Int) {
        pixelCanvasRepository.resizeCanvas(newWidth, newHeight)
    }
}