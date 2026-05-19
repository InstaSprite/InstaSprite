package com.instasprite.app.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.PixelCanvas
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.blendPixel
import com.instasprite.app.utils.pixelToTileCoord
import com.instasprite.app.utils.pixelsToTiles
import com.instasprite.app.utils.tilesToPixels
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class PixelCanvasRepository(var model: PixelCanvas) {
    private val transparentArgb = Color.Transparent.toArgb()

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

    var selectionMask: BooleanArray? = null

    init {
        if (_layers.isEmpty()) {
            addLayer("Layer 1")
        }
    }

    private fun getActiveLayerIndex(): Int {
        val index = _layers.indexOfFirst { it.id == activeLayerId }
        return if (index >= 0) index else 0
    }

    private fun deepCopyTiles(source: Map<TileCoord, IntArray>): Map<TileCoord, IntArray> {
        if (source.isEmpty()) return emptyMap()
        return source.mapValues { it.value.copyOf() }
    }

    private fun isTileEmpty(tile: IntArray): Boolean = tile.all { it == transparentArgb }

    private fun mutableTileCopy(source: Map<TileCoord, IntArray>): LinkedHashMap<TileCoord, IntArray> {
        return LinkedHashMap<TileCoord, IntArray>(source.size).apply {
            for ((coord, pixels) in source) {
                put(coord, pixels.copyOf())
            }
        }
    }

    private fun composeTileIntoBuffer(
        tileCoord: TileCoord,
        tilePixels: IntArray,
        buffer: IntArray,
        bufferWidth: Int,
        bufferHeight: Int,
        layerOpacity: Float = 1.0f,
        blendMode: BlendMode = BlendMode.NORMAL,
        startRow: Int = 0,
        startCol: Int = 0,
        regionWidth: Int = bufferWidth,
        regionHeight: Int = bufferHeight
    ) {
        val originX = tileCoord.x * TILE_SIZE
        val originY = tileCoord.y * TILE_SIZE
        val rowStart = maxOf(startRow, originY)
        val rowEnd = minOf(startRow + regionHeight, originY + TILE_SIZE, bufferHeight)
        val colStart = maxOf(startCol, originX)
        val colEnd = minOf(startCol + regionWidth, originX + TILE_SIZE, bufferWidth)
        if (rowStart >= rowEnd || colStart >= colEnd) return

        for (row in rowStart until rowEnd) {
            val tileRow = row - originY
            val dstBase = (row - startRow) * regionWidth
            val srcBase = tileRow * TILE_SIZE
            for (col in colStart until colEnd) {
                val argb = tilePixels[srcBase + (col - originX)]
                val dstIdx = dstBase + (col - startCol)
                buffer[dstIdx] = blendPixel(buffer[dstIdx], argb, layerOpacity, blendMode)
            }
        }
    }

    private fun tilePixelAt(tiles: Map<TileCoord, IntArray>, row: Int, col: Int): Int {
        val coord = pixelToTileCoord(row, col)
        val tile = tiles[coord] ?: return transparentArgb
        val localRow = row - coord.y * TILE_SIZE
        val localCol = col - coord.x * TILE_SIZE
        return tile[localRow * TILE_SIZE + localCol]
    }

    private fun setTilePixel(
        tiles: Map<TileCoord, IntArray>,
        row: Int,
        col: Int,
        argb: Int,
        blend: Boolean = true
    ): Map<TileCoord, IntArray> {
        if (row !in 0 until height || col !in 0 until width) return tiles

        val coord = pixelToTileCoord(row, col)
        val localRow = row - coord.y * TILE_SIZE
        val localCol = col - coord.x * TILE_SIZE
        val mutable = mutableTileCopy(tiles)
        val tile = mutable[coord]?.copyOf() ?: IntArray(TILE_SIZE * TILE_SIZE)
        val dstColor = tile[localRow * TILE_SIZE + localCol]
        val newColor = if (blend) blendPixel(dstColor, argb, 1f) else argb
        tile[localRow * TILE_SIZE + localCol] = newColor

        if (isTileEmpty(tile)) {
            mutable.remove(coord)
        } else {
            mutable[coord] = tile
        }
        return mutable
    }

    private fun setLayerPixel(layer: Layer, row: Int, col: Int, argb: Int, blend: Boolean = true): Layer {
        val updatedTiles = setTilePixel(layer.tiles, row, col, argb, blend)
        return layer.copy(tiles = updatedTiles)
    }

    private fun mergeTiles(
        bottom: Map<TileCoord, IntArray>,
        top: Map<TileCoord, IntArray>,
        topOpacity: Float = 1.0f,
        topBlendMode: BlendMode = BlendMode.NORMAL
    ): Map<TileCoord, IntArray> {
        if (bottom.isEmpty() && top.isEmpty()) return emptyMap()
        val result = mutableTileCopy(bottom)
        for ((coord, topTile) in top) {
            val bottomTile = result[coord]?.copyOf() ?: IntArray(TILE_SIZE * TILE_SIZE)
            var changed = false
            for (i in topTile.indices) {
                val blended = blendPixel(bottomTile[i], topTile[i], topOpacity, topBlendMode)
                if (blended != bottomTile[i]) {
                    bottomTile[i] = blended
                    changed = true
                }
            }
            if (changed) {
                if (isTileEmpty(bottomTile)) result.remove(coord) else result[coord] = bottomTile
            }
        }
        return result
    }

    private fun inflateLayerPixels(layer: Layer): IntArray {
        return tilesToPixels(layer.tiles, width, height)
    }

    fun addLayer(name: String) {
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = name,
            isVisible = true,
            isLocked = false,
            tiles = emptyMap()
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
            val merged =
                mergeTiles(bottomLayer.tiles, topLayer.tiles, topLayer.opacity, topLayer.blendMode)
            _layers[index - 1] = bottomLayer.copy(tiles = merged)
            _layers.removeAt(index)
            if (activeLayerId == id) {
                activeLayerId = _layers[index - 1].id
            }
        }
    }

    fun reorderLayer(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _layers.indices || toIndex !in _layers.indices) return
        val layer = _layers.removeAt(fromIndex)
        _layers.add(toIndex, layer)
    }

    fun rotate() {
        val oldWidth = width
        val oldHeight = height
        val rotatedCanvasWidth = oldHeight
        val rotatedCanvasHeight = oldWidth

        for (i in _layers.indices) {
            val source = inflateLayerPixels(_layers[i])
            val rotated = IntArray(rotatedCanvasWidth * rotatedCanvasHeight) { transparentArgb }
            for (row in 0 until oldHeight) {
                for (col in 0 until oldWidth) {
                    val argb = source[row * oldWidth + col]
                    if (argb == transparentArgb) continue
                    val newRow = col
                    val newCol = oldHeight - 1 - row
                    rotated[newRow * rotatedCanvasWidth + newCol] = argb
                }
            }
            _layers[i] = _layers[i].copy(
                tiles = pixelsToTiles(
                    rotated,
                    rotatedCanvasWidth,
                    rotatedCanvasHeight
                )
            )
        }

        width = rotatedCanvasWidth
        height = rotatedCanvasHeight
    }

    fun horizontalFlip() {
        for (i in _layers.indices) {
            val source = inflateLayerPixels(_layers[i])
            val flipped = IntArray(width * height) { transparentArgb }
            for (row in 0 until height) {
                for (col in 0 until width) {
                    val argb = source[row * width + col]
                    if (argb == transparentArgb) continue
                    flipped[row * width + (width - 1 - col)] = argb
                }
            }
            _layers[i] = _layers[i].copy(tiles = pixelsToTiles(flipped, width, height))
        }
    }

    fun verticalFlip() {
        for (i in _layers.indices) {
            val source = inflateLayerPixels(_layers[i])
            val flipped = IntArray(width * height) { transparentArgb }
            for (row in 0 until height) {
                for (col in 0 until width) {
                    val argb = source[row * width + col]
                    if (argb == transparentArgb) continue
                    flipped[(height - 1 - row) * width + col] = argb
                }
            }
            _layers[i] = _layers[i].copy(tiles = pixelsToTiles(flipped, width, height))
        }
    }

    fun resizeCanvas(newWidth: Int, newHeight: Int) {
        if (newWidth <= 0 || newHeight <= 0) return

        for (i in _layers.indices) {
            val source = inflateLayerPixels(_layers[i])
            val resized = IntArray(newWidth * newHeight) { transparentArgb }
            val copyWidth = minOf(width, newWidth)
            val copyHeight = minOf(height, newHeight)
            for (row in 0 until copyHeight) {
                for (col in 0 until copyWidth) {
                    resized[row * newWidth + col] = source[row * width + col]
                }
            }
            _layers[i] = _layers[i].copy(tiles = pixelsToTiles(resized, newWidth, newHeight))
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
            _layers.addAll(newLayers.map { it.copy() })
            activeLayerId = _layers.last().id
        }
    }

    fun setPixel(row: Int, col: Int, color: Color, blend: Boolean = true) {
        if (row in 0 until height && col in 0 until width) {
            if (selectionMask?.get(row * width + col) == false) return
            
            val index = getActiveLayerIndex()
            if (index !in _layers.indices) return
            val layer = _layers[index]
            if (!layer.isLocked && layer.isVisible) {
                _layers[index] = setLayerPixel(layer, row, col, color.toArgb(), blend)
            }
        }
    }

    fun setPixel(row: Int, col: Int, color: Color, scale: Int, blend: Boolean = true) {
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
                setPixel(r, c, color, blend)
            }
        }
    }

    fun getPixel(row: Int, col: Int): Color {
        if (row in 0 until height && col in 0 until width) {
            val idx = getActiveLayerIndex()
            if (idx in _layers.indices) {
                return Color(tilePixelAt(_layers[idx].tiles, row, col))
            }
        }
        return Color.Transparent
    }

    fun getAllPixels(): IntArray {
        val composited = IntArray(width * height) { transparentArgb }
        for (layer in _layers) {
            if (!layer.isVisible) continue
            val layerOpacity = layer.opacity
            val layerBlendMode = layer.blendMode
            for ((coord, tilePixels) in layer.tiles) {
                val originX = coord.x * TILE_SIZE
                val originY = coord.y * TILE_SIZE
                for (localRow in 0 until TILE_SIZE) {
                    val canvasRow = originY + localRow
                    if (canvasRow !in 0 until height) continue
                    val dstBase = canvasRow * width
                    val srcBase = localRow * TILE_SIZE
                    for (localCol in 0 until TILE_SIZE) {
                        val canvasCol = originX + localCol
                        if (canvasCol !in 0 until width) continue
                        val argb = tilePixels[srcBase + localCol]
                        val dstIdx = dstBase + canvasCol
                        composited[dstIdx] =
                            blendPixel(composited[dstIdx], argb, layerOpacity, layerBlendMode)
                    }
                }
            }
        }
        return composited
    }

    fun getCompositedPixelAt(row: Int, col: Int): Int {
        if (row !in 0 until height || col !in 0 until width) return transparentArgb
        var color = transparentArgb
        for (layer in _layers) {
            if (!layer.isVisible) continue
            val argb = tilePixelAt(layer.tiles, row, col)
            color = blendPixel(color, argb, layer.opacity, layer.blendMode)
        }
        return color
    }

    fun getPreviewCompositedPixelAt(row: Int, col: Int, overlayColor: Int, blend: Boolean = true): Int {
        if (row !in 0 until height || col !in 0 until width) return transparentArgb
        var color = transparentArgb
        val activeIdx = getActiveLayerIndex()
        for (i in _layers.indices) {
            val layer = _layers[i]
            if (!layer.isVisible) continue
            val argb = if (i == activeIdx) {
                val originalPixel = tilePixelAt(layer.tiles, row, col)
                if (blend) blendPixel(originalPixel, overlayColor, 1f) else overlayColor
            } else {
                tilePixelAt(layer.tiles, row, col)
            }
            color = blendPixel(color, argb, layer.opacity, layer.blendMode)
        }
        return color
    }

    fun getAllPixelsInRegion(
        startRow: Int, startCol: Int,
        regionHeight: Int, regionWidth: Int
    ): IntArray {
        val result = IntArray(regionWidth * regionHeight) { transparentArgb }
        if (regionHeight <= 0 || regionWidth <= 0) return result

        val regionEndRow = startRow + regionHeight
        val regionEndCol = startCol + regionWidth

        for (layer in _layers) {
            if (!layer.isVisible) continue
            val layerOpacity = layer.opacity
            val layerBlendMode = layer.blendMode
            for ((coord, tilePixels) in layer.tiles) {
                val originX = coord.x * TILE_SIZE
                val originY = coord.y * TILE_SIZE
                val startCanvasRow = maxOf(startRow, originY)
                val endCanvasRow = minOf(regionEndRow, originY + TILE_SIZE, height)
                val startCanvasCol = maxOf(startCol, originX)
                val endCanvasCol = minOf(regionEndCol, originX + TILE_SIZE, width)
                if (startCanvasRow >= endCanvasRow || startCanvasCol >= endCanvasCol) continue

                for (row in startCanvasRow until endCanvasRow) {
                    val tileRow = row - originY
                    val dstBase = (row - startRow) * regionWidth
                    val srcBase = tileRow * TILE_SIZE
                    for (col in startCanvasCol until endCanvasCol) {
                        val argb = tilePixels[srcBase + (col - originX)]
                        val dstIdx = dstBase + (col - startCol)
                        result[dstIdx] =
                            blendPixel(result[dstIdx], argb, layerOpacity, layerBlendMode)
                    }
                }
            }
        }

        return result
    }

    fun setAllPixels(pixels: IntArray) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (!layer.isLocked && pixels.size == width * height) {
            if (selectionMask != null) {
                val currentPixels = tilesToPixels(layer.tiles, width, height)
                var changed = false
                for (i in pixels.indices) {
                    if (selectionMask!![i] && currentPixels[i] != pixels[i]) {
                        currentPixels[i] = pixels[i]
                        changed = true
                    }
                }
                if (changed) {
                    _layers[idx] = layer.copy(tiles = pixelsToTiles(currentPixels, width, height))
                }
            } else {
                _layers[idx] = layer.copy(tiles = pixelsToTiles(pixels, width, height))
            }
        }
    }

    fun batchSetPixels(indices: IntArray, colors: IntArray, count: Int, blend: Boolean = true) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (layer.isLocked || !layer.isVisible || count <= 0) return

        val writeCount = minOf(count, indices.size, colors.size)
        if (writeCount <= 0) return

        val updatedTiles = LinkedHashMap(layer.tiles)
        val copiedTiles = HashSet<TileCoord>()
        val touchedTiles = HashSet<TileCoord>()

        for (i in 0 until writeCount) {
            val pixelIndex = indices[i]
            if (pixelIndex !in 0 until (width * height)) continue
            if (selectionMask?.get(pixelIndex) == false) continue

            val row = pixelIndex / width
            val col = pixelIndex % width
            val color = colors[i]

            val coord = pixelToTileCoord(row, col)
            touchedTiles.add(coord)

            var tile = updatedTiles[coord]
            if (tile == null) {
                if (color == transparentArgb) continue
                tile = IntArray(TILE_SIZE * TILE_SIZE)
                updatedTiles[coord] = tile
                copiedTiles.add(coord)
            } else if (coord !in copiedTiles) {
                tile = tile.copyOf()
                updatedTiles[coord] = tile
                copiedTiles.add(coord)
            }

            val localRow = row - coord.y * TILE_SIZE
            val localCol = col - coord.x * TILE_SIZE
            val dstColor = tile[localRow * TILE_SIZE + localCol]
            val newColor = if (blend) blendPixel(dstColor, color, 1f) else color
            tile[localRow * TILE_SIZE + localCol] = newColor
        }

        for (coord in touchedTiles) {
            val tile = updatedTiles[coord] ?: continue
            if (isTileEmpty(tile)) {
                updatedTiles.remove(coord)
            }
        }

        _layers[idx] = layer.copy(tiles = updatedTiles)
    }

    fun getActiveLayerPixelsDirect(): IntArray? {
        val index = getActiveLayerIndex()
        if (index !in _layers.indices) return null
        val layer = _layers[index]
        if (layer.isLocked || !layer.isVisible) return null
        return tilesToPixels(layer.tiles, width, height)
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
            tiles = if (pixelsData != null && pixelsData.size == width * height) {
                pixelsToTiles(pixelsData, width, height)
            } else {
                emptyMap()
            }
        )

        _layers.add(newLayer)
        activeLayerId = newLayer.id
    }

    fun getSprite(): Sprite {
        return Sprite(
            width = width,
            height = height,
            layers = _layers.map { it.copy() }
        )
    }

    fun setLayerOpacity(id: String, opacity: Float) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val layer = _layers[index]
            _layers[index] = layer.copy(opacity = opacity.coerceIn(0f, 1f))
        }
    }

    fun setLayerBlendMode(id: String, mode: BlendMode) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index != -1) {
            val layer = _layers[index]
            _layers[index] = layer.copy(blendMode = mode)
        }
    }
}