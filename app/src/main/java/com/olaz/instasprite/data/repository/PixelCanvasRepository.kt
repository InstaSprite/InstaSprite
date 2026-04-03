package com.olaz.instasprite.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.domain.model.Cel
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.PixelCanvas
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.tool.PixelChange
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class PixelCanvasRepository(var model: PixelCanvas) {
    var width: Int
        get() = model.width
        set(value) {
            model.width = value
        }

    var height: Int
        get() = model.height
        set(value) {
            model.height = value
        }

    private val _layers = CopyOnWriteArrayList<Layer>()
    val layers: List<Layer> get() = _layers

    var activeLayerId: String = ""
        private set

    init {
        if (_layers.isEmpty()) {
            addLayer("Layer 1")
        }
    }

    private fun getActiveLayerIndex(): Int {
        val index = _layers.indexOfFirst { it.id == activeLayerId }
        return if (index >= 0) index else 0
    }

    fun addLayer(name: String) {
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = name,
            isVisible = true,
            isLocked = false,
            cel = Cel(
                x = 0,
                y = 0,
                width = width,
                height = height,
                pixels = IntArray(width * height) { Color.Transparent.toArgb() }
            )
        )

        _layers.add(newLayer)
        activeLayerId = newLayer.id
    }

    fun removeLayer(id: String) {
        if (_layers.size > 1) {
            val index = _layers.indexOfFirst { it.id == id }
            if (index != -1) {
                _layers.removeAt(index)
                if (activeLayerId == id) {
                    activeLayerId = _layers.last().id
                }
            }
        }
    }

    fun setActiveLayer(id: String) {
        if (_layers.any { it.id == id }) {
            activeLayerId = id
        }
    }

    fun toggleVisibility(id: String) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val layer = _layers[index]
            _layers[index] = layer.copy(isVisible = !layer.isVisible)
        }
    }

    fun toggleLock(id: String) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val layer = _layers[index]
            _layers[index] = layer.copy(isLocked = !layer.isLocked)
        }
    }

    fun mergeLayerDown(id: String) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index > 0) {
            val topLayer = _layers[index]
            val bottomLayer = _layers[index - 1]
            val mergedPixels = IntArray(width * height)
            val topPixels = topLayer.cel.pixels
            val bottomPixels = bottomLayer.cel.pixels

            for (i in mergedPixels.indices) {
                if (topPixels[i] != 0) {
                    mergedPixels[i] = topPixels[i]
                } else {
                    mergedPixels[i] = bottomPixels[i]
                }
            }

            _layers[index - 1] = bottomLayer.copy(cel = bottomLayer.cel.copy(pixels = mergedPixels))
            _layers.removeAt(index)
            if (activeLayerId == id) {
                activeLayerId = _layers[index - 1].id
            }
        }
    }

    fun reorderLayer(fromIndex: Int, toIndex: Int) {
        val layer = _layers.removeAt(fromIndex)
        _layers.add(toIndex, layer)
    }

    fun rotate() {
        val oldWidth = width
        val oldHeight = height

        for (i in _layers.indices) {
            val layer = _layers[i]
            val rotatedPixels = IntArray(oldHeight * oldWidth) { Color.Transparent.toArgb() }
            val sourcePixels = layer.cel.pixels
            for (row in 0 until oldHeight) {
                for (col in 0 until oldWidth) {
                    val newCol = oldHeight - 1 - row
                    val newIndex = col * oldHeight + newCol
                    val oldIndex = row * oldWidth + col
                    if (newIndex in rotatedPixels.indices && oldIndex in sourcePixels.indices) {
                        rotatedPixels[newIndex] = sourcePixels[oldIndex]
                    }
                }
            }
            _layers[i] = layer.copy(cel = layer.cel.copy(width = oldHeight, height = oldWidth, pixels = rotatedPixels))
        }
        width = oldHeight
        height = oldWidth
    }

    fun horizontalFlip() {
        for (i in _layers.indices) {
            val layer = _layers[i]
            val flipped = IntArray(width * height) { Color.Transparent.toArgb() }
            val sourcePixels = layer.cel.pixels
            for (row in 0 until height) {
                for (col in 0 until width) {
                    flipped[row * width + (width - 1 - col)] = sourcePixels[row * width + col]
                }
            }
            _layers[i] = layer.copy(cel = layer.cel.copy(pixels = flipped))

        }
    }

    fun verticalFlip() {
        for (i in _layers.indices) {
            val layer = _layers[i]
            val flipped = IntArray(width * height) { Color.Transparent.toArgb() }
            val sourcePixels = layer.cel.pixels
            for (row in 0 until height) {
                for (col in 0 until width) {
                    flipped[(height - 1 - row) * width + col] = sourcePixels[row * width + col]
                }
            }
            _layers[i] = layer.copy(cel = layer.cel.copy(pixels = flipped))

        }
    }

    fun resizeCanvas(newWidth: Int, newHeight: Int) {
        val oldWidth = this.width
        val oldHeight = this.height

        val copyWidth = minOf(oldWidth, newWidth)
        val copyHeight = minOf(oldHeight, newHeight)

        for (i in _layers.indices) {
            val layer = _layers[i]
            val newPixels = IntArray(newWidth * newHeight) { Color.Transparent.toArgb() }
            val sourcePixels = layer.cel.pixels
            for (row in 0 until copyHeight) {
                for (col in 0 until copyWidth) {
                    val oldIndex = row * oldWidth + col
                    val newIndex = row * newWidth + col
                    newPixels[newIndex] = sourcePixels[oldIndex]
                }
            }
            _layers[i] = layer.copy(cel = layer.cel.copy(width = newWidth, height = newHeight, pixels = newPixels))
        }
        
        this.width = newWidth
        this.height = newHeight
    }

    fun setCanvasData(width: Int, height: Int, newLayers: List<Layer>) {
        this.width = width
        this.height = height
        _layers.clear()

        if (newLayers.isEmpty()) {
            addLayer("Layer 1")
        } else {
            _layers.addAll(newLayers.map {
                it.copy(cel = it.cel.copy(pixels = it.cel.pixels.copyOf()))
            })
            activeLayerId = _layers.last().id
        }
    }

    fun setPixel(row: Int, col: Int, color: Color) {
        if (row in 0 until height && col in 0 until width) {
            val index = getActiveLayerIndex()
            if (index < 0 || index >= _layers.size) return
            val layer = _layers[index]
            if (!layer.isLocked && layer.isVisible) {
                layer.cel.pixels[row * width + col] = color.toArgb()
            }
        }
    }

    fun setPixel(row: Int, col: Int, color: Color, scale: Int) {
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

        for (r in xStart..xEnd) {
            for (c in yStart..yEnd) {
                setPixel(r, c, color)
            }
        }
    }

    fun getPixel(row: Int, col: Int): Color {
        if (row in 0 until height && col in 0 until width) {
            val idx = getActiveLayerIndex()
            if (idx in _layers.indices) {
                val layer = _layers[idx]
                return Color(layer.cel.pixels[row * width + col])
            }
        }
        return Color.Transparent
    }

    fun getAllPixels(): IntArray {
        val composited = IntArray(width * height) { Color.Transparent.toArgb() }
        for (layer in _layers) {
            if (layer.isVisible) {
                for (i in layer.cel.pixels.indices) {
                    val layerColor = layer.cel.pixels[i]
                    if (layerColor != Color.Transparent.toArgb()) {
                        composited[i] = layerColor
                    }
                }
            }
        }
        return composited
    }

    fun getCompositedPixelAt(row: Int, col: Int): Int {
        if (row !in 0 until height || col !in 0 until width) return Color.Transparent.toArgb()
        val idx = row * width + col
        var color = Color.Transparent.toArgb()
        for (layer in _layers) {
            if (layer.isVisible && layer.cel.pixels[idx] != 0) {
                color = layer.cel.pixels[idx]
            }
        }
        return color
    }

    fun filterVisibleChanges(changes: List<PixelChange>): List<PixelChange> {
        val activeIndex = getActiveLayerIndex()
        if (activeIndex >= _layers.lastIndex) return changes

        val layersAbove = _layers.subList(activeIndex + 1, _layers.size).filter { it.isVisible }
        if (layersAbove.isEmpty()) return changes

        val visibleChanges = ArrayList<PixelChange>(changes.size)
        for (change in changes) {
            val r = change.row
            val c = change.col
            if (r !in 0 until height || c !in 0 until width) continue

            val idx = r * width + c
            var occluded = false
            for (i in layersAbove.indices) {
                if (layersAbove[i].cel.pixels[idx] != 0) {
                    occluded = true
                    break
                }
            }
            if (!occluded) {
                visibleChanges.add(change)
            }
        }
        return visibleChanges
    }

    fun getAllPixelsInRegion(
        startRow: Int, startCol: Int,
        regionHeight: Int, regionWidth: Int
    ): IntArray {
        val result = IntArray(regionWidth * regionHeight)
        for (r in 0 until regionHeight) {
            val canvasRow = startRow + r
            if (canvasRow !in 0 until height) continue
            for (c in 0 until regionWidth) {
                val canvasCol = startCol + c
                if (canvasCol !in 0 until width) continue
                val idx = canvasRow * width + canvasCol
                var color = Color.Transparent.toArgb()
                for (layer in _layers) {
                    if (layer.isVisible && layer.cel.pixels[idx] != 0) {
                        color = layer.cel.pixels[idx]
                    }
                }
                result[r * regionWidth + c] = color
            }
        }
        return result
    }

    fun setAllPixels(pixels: IntArray) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (!layer.isLocked && pixels.size == layer.cel.pixels.size) {
            System.arraycopy(pixels, 0, layer.cel.pixels, 0, pixels.size)
        }
    }

    fun batchSetPixels(changes: List<PixelChange>) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (layer.isLocked || !layer.isVisible) return
        for (change in changes) {
            if (change.row in 0 until height && change.col in 0 until width) {
                layer.cel.pixels[change.row * width + change.col] = change.color
            }
        }
    }

    fun getActiveLayerPixelsDirect(): IntArray? {
        val index = getActiveLayerIndex()
        if (index !in _layers.indices) return null
        val layer = _layers[index]
        if (layer.isLocked || !layer.isVisible) return null
        return layer.cel.pixels
    }

    fun setCanvas(width: Int, height: Int, pixelsData: IntArray? = null) {
        this.width = width
        this.height = height
        _layers.clear()

        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Layer 1",
            isVisible = true,
            isLocked = false,
            cel = Cel(
                x = 0,
                y = 0,
                width = width,
                height = height,
                pixels = if (pixelsData != null && pixelsData.size == width * height) {
                    pixelsData.copyOf()
                } else {
                    IntArray(width * height) { Color.Transparent.toArgb() }
                }
            )
        )
        _layers.add(newLayer)
        activeLayerId = newLayer.id
    }

    fun getSprite(): Sprite {
        return Sprite(
            width = width,
            height = height,
            layers = _layers.map { it.copy(cel = it.cel.copy(pixels = it.cel.pixels.copyOf())) }
        )
    }
}