package com.instasprite.app.domain.usecase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.PixelCanvasRepository
import com.instasprite.app.domain.canvashistory.TileChangeTracker
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.pixelToTileCoord
import java.util.LinkedHashMap

class PixelCanvasUseCase(
    private val pixelCanvasRepository: PixelCanvasRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
) {
    private var tileChangeTracker: TileChangeTracker? = null

    fun getLayers(): List<Layer> = pixelCanvasRepository.layers
    fun getActiveLayerId(): String = pixelCanvasRepository.activeLayerId
    fun addLayer(name: String) = pixelCanvasRepository.addLayer(name)
    fun removeLayer(id: String) = pixelCanvasRepository.removeLayer(id)
    fun setActiveLayer(id: String) = pixelCanvasRepository.setActiveLayer(id)
    fun toggleVisibility(id: String) = pixelCanvasRepository.toggleVisibility(id)
    fun toggleLock(id: String) = pixelCanvasRepository.toggleLock(id)
    fun mergeLayerDown(id: String) = pixelCanvasRepository.mergeLayerDown(id)
    fun reorderLayer(fromIndex: Int, toIndex: Int) = pixelCanvasRepository.reorderLayer(fromIndex, toIndex)
    fun setLayerOpacity(id: String, opacity: Float) = pixelCanvasRepository.setLayerOpacity(id, opacity)
    fun setLayerBlendMode(id: String, mode: com.instasprite.app.domain.model.BlendMode) = pixelCanvasRepository.setLayerBlendMode(id, mode)

    fun getCanvasWidth(): Int {
        return pixelCanvasRepository.width
    }

    fun getCanvasHeight(): Int {
        return pixelCanvasRepository.height
    }

    fun beginTileHistory(tracker: TileChangeTracker) {
        tileChangeTracker = tracker
    }

    fun endTileHistory() {
        tileChangeTracker = null
    }

    fun setPixel(row: Int, col: Int, color: Color) {
        captureActiveTileBeforeMutation(row, col)
        pixelCanvasRepository.setPixel(row, col, color)
    }

    fun setPixel(row: Int, col: Int, color: Color, scale: Int) {
        captureTileSpanBeforeMutation(row, col, scale)
        pixelCanvasRepository.setPixel(row, col, color, scale)
    }

    fun getPixel(row: Int, col: Int): Color {
        return pixelCanvasRepository.getPixel(row, col)
    }

    fun setCanvas(width: Int, height: Int, pixels: IntArray? = null) {
        pixelCanvasRepository.setCanvas(width, height, pixels)
    }

    fun setSelectionMask(mask: BooleanArray?) {
        pixelCanvasRepository.selectionMask = mask
    }

    fun getSelectionMask(): BooleanArray? {
        return pixelCanvasRepository.selectionMask
    }

    fun setCanvas(sprite: Sprite) {
        pixelCanvasRepository.setCanvasData(sprite.width, sprite.height, sprite.layers)
    }

    fun setAllPixels(pixels: IntArray) {
        captureActiveLayerReplacement(pixels)
        pixelCanvasRepository.setAllPixels(pixels)
    }

    fun batchSetPixels(indices: IntArray, colors: IntArray, count: Int) {
        captureBatchPixelChanges(indices, colors.size, count)
        pixelCanvasRepository.batchSetPixels(indices, colors, count)
    }

    fun getActiveLayerPixelsDirect(): IntArray? = pixelCanvasRepository.getActiveLayerPixelsDirect()

    fun getAllPixels(): IntArray {
        return pixelCanvasRepository.getAllPixels()
    }

    fun getCompositedPixelAt(row: Int, col: Int): Int {
        return pixelCanvasRepository.getCompositedPixelAt(row, col)
    }

    fun getPreviewCompositedPixelAt(row: Int, col: Int, overlayColor: Int): Int {
        return pixelCanvasRepository.getPreviewCompositedPixelAt(row, col, overlayColor)
    }

    fun getAllPixelsInRegion(
        startRow: Int, startCol: Int,
        regionHeight: Int, regionWidth: Int
    ): IntArray {
        return pixelCanvasRepository.getAllPixelsInRegion(startRow, startCol, regionHeight, regionWidth)
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

    private fun captureActiveTileBeforeMutation(row: Int, col: Int) {
        val tracker = tileChangeTracker ?: return
        val layer = pixelCanvasRepository.layers.firstOrNull { it.id == pixelCanvasRepository.activeLayerId } ?: return
        if (layer.isLocked || !layer.isVisible) return
        if (row !in 0 until pixelCanvasRepository.height || col !in 0 until pixelCanvasRepository.width) return

        val coord = pixelToTileCoord(row, col)
        tracker.captureIfNeeded(layer.id, coord, layer.tiles[coord] ?: emptyTile())
    }

    private fun captureTileSpanBeforeMutation(row: Int, col: Int, scale: Int) {
        if (scale <= 1) {
            captureActiveTileBeforeMutation(row, col)
            return
        }

        val tracker = tileChangeTracker ?: return
        val layer = pixelCanvasRepository.layers.firstOrNull { it.id == pixelCanvasRepository.activeLayerId } ?: return
        if (layer.isLocked || !layer.isVisible) return
        val width = pixelCanvasRepository.width
        val height = pixelCanvasRepository.height

        var xStart = row
        var xEnd = row
        var yStart = col
        var yEnd = col

        for (s in 2..scale) {
            if (s % 2 == 0) {
                xStart -= 1
                yStart -= 1
            } else {
                xEnd += 1
                yEnd += 1
            }
        }

        xStart = xStart.coerceAtLeast(0)
        yStart = yStart.coerceAtLeast(0)
        xEnd = xEnd.coerceAtMost(height - 1)
        yEnd = yEnd.coerceAtMost(width - 1)

        if (xStart > xEnd || yStart > yEnd) return

        val seenCoords = LinkedHashMap<TileCoord, Boolean>()
        for (r in xStart..xEnd) {
            for (c in yStart..yEnd) {
                val coord = pixelToTileCoord(r, c)
                if (seenCoords.putIfAbsent(coord, true) != null) continue
                tracker.captureIfNeeded(layer.id, coord, layer.tiles[coord] ?: emptyTile())
            }
        }
    }

    private fun captureBatchPixelChanges(indices: IntArray, colorsSize: Int, count: Int) {
        val tracker = tileChangeTracker ?: return
        val layer = pixelCanvasRepository.layers.firstOrNull { it.id == pixelCanvasRepository.activeLayerId } ?: return
        if (layer.isLocked || !layer.isVisible) return
        val writeCount = minOf(count, indices.size, colorsSize)
        if (writeCount <= 0) return

        val seenCoords = LinkedHashMap<TileCoord, Boolean>()
        for (i in 0 until writeCount) {
            val pixelIndex = indices[i]
            if (pixelIndex !in 0 until (pixelCanvasRepository.width * pixelCanvasRepository.height)) continue

            val row = pixelIndex / pixelCanvasRepository.width
            val col = pixelIndex % pixelCanvasRepository.width
            val coord = pixelToTileCoord(row, col)
            if (seenCoords.putIfAbsent(coord, true) != null) continue
            tracker.captureIfNeeded(layer.id, coord, layer.tiles[coord] ?: emptyTile())
        }
    }

    private fun captureActiveLayerReplacement(newPixels: IntArray) {
        val tracker = tileChangeTracker ?: return
        val layer = pixelCanvasRepository.layers.firstOrNull { it.id == pixelCanvasRepository.activeLayerId } ?: return
        if (layer.isLocked || !layer.isVisible) return
        val width = pixelCanvasRepository.width
        val height = pixelCanvasRepository.height
        if (width <= 0 || height <= 0 || newPixels.size != width * height) return

        val currentPixels = pixelCanvasRepository.getActiveLayerPixelsDirect() ?: return
        val seenCoords = LinkedHashMap<TileCoord, Boolean>()
        val tileCols = (width + TILE_SIZE - 1) / TILE_SIZE
        val tileRows = (height + TILE_SIZE - 1) / TILE_SIZE

        for (tileY in 0 until tileRows) {
            for (tileX in 0 until tileCols) {
                val coord = TileCoord(tileX, tileY)
                val originX = tileX * TILE_SIZE
                val originY = tileY * TILE_SIZE
                var changed = false

                for (localRow in 0 until TILE_SIZE) {
                    val canvasRow = originY + localRow
                    if (canvasRow >= height) break
                    val srcBase = canvasRow * width
                    for (localCol in 0 until TILE_SIZE) {
                        val canvasCol = originX + localCol
                        if (canvasCol >= width) break
                        val index = srcBase + canvasCol
                        if (currentPixels[index] != newPixels[index]) {
                            changed = true
                            break
                        }
                    }
                    if (changed) break
                }

                if (changed && seenCoords.putIfAbsent(coord, true) == null) {
                    tracker.captureIfNeeded(layer.id, coord, layer.tiles[coord] ?: emptyTile())
                }
            }
        }
    }

    private fun emptyTile(): IntArray = IntArray(TILE_SIZE * TILE_SIZE)
}