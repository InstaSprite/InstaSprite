package com.olaz.instasprite.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.PixelCanvas
import com.olaz.instasprite.domain.model.Sprite
import java.util.UUID

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

    private val _layers = mutableListOf<Layer>()
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
            pixels = MutableList(width * height) { Color.Transparent.toArgb() }
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

            for (i in mergedPixels.indices) {
                if (topLayer.pixels[i] != 0) {
                    mergedPixels[i] = topLayer.pixels[i]
                } else {
                    mergedPixels[i] = bottomLayer.pixels[i]
                }
            }

            val newLayer = bottomLayer.copy(pixels = mergedPixels.toMutableList())
            _layers[index - 1] = newLayer
            _layers.removeAt(index)
            if (activeLayerId == id) {
                activeLayerId = newLayer.id
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
            for (row in 0 until oldHeight) {
                for (col in 0 until oldWidth) {
                    val newCol = oldHeight - 1 - row
                    val newIndex = col * oldHeight + newCol
                    val oldIndex = row * oldWidth + col
                    if (newIndex in rotatedPixels.indices && oldIndex in layer.pixels.indices) {
                        rotatedPixels[newIndex] = layer.pixels[oldIndex]
                    }
                }
            }
            _layers[i] = layer.copy(pixels = rotatedPixels.toMutableList())
        }
        width = oldHeight
        height = oldWidth
    }

    fun horizontalFlip() {
        for (i in _layers.indices) {
            val layer = _layers[i]
            val flipped = IntArray(width * height) { Color.Transparent.toArgb() }
            for (row in 0 until height) {
                for (col in 0 until width) {
                    flipped[row * width + (width - 1 - col)] = layer.pixels[row * width + col]
                }
            }
            _layers[i] = layer.copy(pixels = flipped.toMutableList())

        }
    }

    fun verticalFlip() {
        for (i in _layers.indices) {
            val layer = _layers[i]
            val flipped = IntArray(width * height) { Color.Transparent.toArgb() }
            for (row in 0 until height) {
                for (col in 0 until width) {
                    flipped[(height - 1 - row) * width + col] = layer.pixels[row * width + col]
                }
            }
            _layers[i] = layer.copy(pixels = flipped.toMutableList())

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
            for (row in 0 until copyHeight) {
                for (col in 0 until copyWidth) {
                    val oldIndex = row * oldWidth + col
                    val newIndex = row * newWidth + col
                    newPixels[newIndex] = layer.pixels[oldIndex]
                }
            }
            _layers[i] = layer.copy(pixels = newPixels.toMutableList())
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
                it.copy(pixels = it.pixels.toMutableList())
            })
            // Default to top layer active
            activeLayerId = _layers.last().id
        }
    }

    fun setPixel(row: Int, col: Int, color: Color) {
        if (row in 0 until height && col in 0 until width) {
            val index = getActiveLayerIndex()
            if (index < 0 || index >= _layers.size) return
            val layer = _layers[index]
            if (!layer.isLocked && layer.isVisible) {
                (layer.pixels as MutableList<Int>)[row * width + col] = color.toArgb()
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
                return Color(layer.pixels[row * width + col])
            }
        }
        return Color.Transparent
    }

    fun getAllPixels(): List<Color> {
        val composited = IntArray(width * height) { Color.Transparent.toArgb() }
        for (layer in _layers) {
            if (layer.isVisible) {
                for (i in layer.pixels.indices) {
                    val layerColor = layer.pixels[i]
                    if (layerColor != Color.Transparent.toArgb()) {
                        composited[i] = layerColor
                    }
                }
            }
        }
        return composited.map { Color(it) }
    }

    fun setAllPixels(colors: List<Color>) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (!layer.isLocked && colors.size == layer.pixels.size) {
            for (i in colors.indices) {
                (layer.pixels as MutableList<Int>)[i] = colors[i].toArgb()
            }
        }
    }

    fun setCanvas(width: Int, height: Int, pixelsData: List<Color>? = null) {
        this.width = width
        this.height = height
        _layers.clear()

        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Layer 1",
            isVisible = true,
            isLocked = false,
            pixels = if (pixelsData != null && pixelsData.size == width * height) {
                pixelsData.map { it.toArgb() }.toMutableList()
            } else {
                MutableList(width * height) { Color.Transparent.toArgb() }
            }
        )
        _layers.add(newLayer)
        activeLayerId = newLayer.id
    }

    fun getSprite(): Sprite {
        return Sprite(
            width = width,
            height = height,
            layers = _layers.map { it.copy(pixels = it.pixels.toList()) }
        )
    }
}