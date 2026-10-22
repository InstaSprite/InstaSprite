package com.instasprite.app.data.repository

import com.instasprite.app.utils.CanvasMemoryGuard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.AnimationCel
import com.instasprite.app.domain.model.AnimationFrame
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.LayerCompositor
import com.instasprite.app.domain.model.canMergeLayerDown
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

    var animation: Animation = Animation()
        private set
    var activeFrameId: String = animation.frames.first().id
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
                animation = prune(animation.copy(cels = animation.cels.filterNot { it.layerId == id },
                    frames = animation.frames.map { it.copy(clippingLayerIds = it.clippingLayerIds - id) }))
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

    fun toggleClipping(id: String) {
        val index = _layers.indexOfFirst { it.id == id }
        if (index < 0 || (index == 0 && !_layers[index].isClipping)) return
        val layer = _layers[index]
        val clipping = !layer.isClipping
        animation = animation.copy(frames = animation.frames.map { frame ->
            if (frame.id != activeFrameId) frame else frame.copy(clippingLayerIds =
                if (clipping) frame.clippingLayerIds + id else frame.clippingLayerIds - id)
        })
        _layers[index] = Layer(layer.id, layer.name, layer.isVisible, layer.isLocked,
            layer.opacity, layer.blendMode, layer.tiles, clipping)
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
        if (!_layers.canMergeLayerDown(id, animation)) return
        val top = _layers[index]
        val bottom = _layers[index - 1]
        val images = animation.images.toMutableMap()
        val cels = animation.cels.filterNot { it.layerId == top.id || it.layerId == bottom.id }.toMutableList()
        val mergedIds = mutableMapOf<Pair<String?, String?>, String>()
        for (frame in animation.frames) {
            val bottomCel = animation.cel(bottom.id, frame.id)
            val topCel = animation.cel(top.id, frame.id)
            if (bottomCel == null && topCel == null) continue
            val dataId = mergedIds.getOrPut(bottomCel?.dataId to topCel?.dataId) {
                val frameLayers = listOf(bottom, top).map { layer ->
                    Layer(layer.id, layer.name, layer.isVisible, layer.isLocked, layer.opacity,
                        layer.blendMode, animation.tiles(layer.id, frame.id), layer.isClipping)
                }
                val merged = mutableMapOf<TileCoord, IntArray>()
                val coordinates = if (top.isClipping) frameLayers[0].tiles.keys
                    else frameLayers.flatMap { it.tiles.keys }.toSet()
                for (coord in coordinates) {
                    val tile = IntArray(TILE_SIZE * TILE_SIZE)
                    if (top.isClipping) {
                        frameLayers[0].tiles.getValue(coord).copyInto(tile)
                        val overlay = frameLayers[1].tiles[coord]
                        if (top.isVisible && overlay != null) {
                            for (i in tile.indices) {
                                tile[i] = LayerCompositor.blendClipped(tile[i], overlay[i], top.opacity, top.blendMode)
                            }
                        }
                    } else {
                        LayerCompositor.render(frameLayers, width, height, tile,
                            coord.y * TILE_SIZE, coord.x * TILE_SIZE, TILE_SIZE, TILE_SIZE)
                    }
                    if (!isTileEmpty(tile)) merged[coord] = tile
                }
                val image = CelImage(tiles = merged)
                images[image.id] = image
                image.id
            }
            cels.add(AnimationCel(layerId = bottom.id, frameId = frame.id, dataId = dataId))
        }
        _layers[index - 1] = if (top.isClipping) bottom else
            bottom.copy(isVisible = true, opacity = 1f, blendMode = BlendMode.NORMAL)
        _layers.removeAt(index)
        animation = prune(animation.copy(cels = cels, images = images,
            frames = animation.frames.map { it.copy(clippingLayerIds = it.clippingLayerIds - id) }))
        if (activeLayerId == id) activeLayerId = bottom.id
        resolveFrame()
    }

    fun reorderLayer(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _layers.indices || toIndex !in _layers.indices) return
        val layer = _layers.removeAt(fromIndex)
        _layers.add(toIndex, layer)
    }

    private fun transformImages(newWidth: Int, newHeight: Int, position: (Int, Int) -> Pair<Int, Int>) {
        CanvasMemoryGuard.requireAvailable(
            CanvasMemoryGuard.estimatePixels(width, height, 4),
            CanvasMemoryGuard.estimatePixels(newWidth, newHeight, 24L + 8L * animation.images.size)
        )
        val oldWidth = width
        val oldHeight = height
        val images = animation.images.mapValues { (_, image) ->
            val source = tilesToPixels(image.tiles, oldWidth, oldHeight)
            val result = IntArray(newWidth * newHeight)
            for (row in 0 until oldHeight) {
                for (col in 0 until oldWidth) {
                    val (targetRow, targetCol) = position(row, col)
                    if (targetRow in 0 until newHeight && targetCol in 0 until newWidth) {
                        result[targetRow * newWidth + targetCol] = source[row * oldWidth + col]
                    }
                }
            }
            CelImage(image.id, pixelsToTiles(result, newWidth, newHeight))
        }
        animation = animation.copy(images = images)
        width = newWidth
        height = newHeight
        resolveFrame()
    }

    fun rotate() {
        val oldHeight = height
        transformImages(height, width) { row, col -> col to oldHeight - 1 - row }
    }

    fun horizontalFlip() = transformImages(width, height) { row, col -> row to width - 1 - col }

    fun verticalFlip() = transformImages(width, height) { row, col -> height - 1 - row to col }

    fun resizeCanvas(newWidth: Int, newHeight: Int) {
        if (newWidth <= 0 || newHeight <= 0) return
        transformImages(newWidth, newHeight) { row, col -> row to col }
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
        animation = Animation.fromLayers(_layers.toList())
        activeFrameId = animation.frames.first().id
    }

    fun setPixel(row: Int, col: Int, color: Color, blend: Boolean = true) {
        if (row in 0 until height && col in 0 until width) {
            if (selectionMask?.get(row * width + col) == false) return
            
            val index = getActiveLayerIndex()
            if (index !in _layers.indices) return
            val layer = _layers[index]
            if (!layer.isLocked && layer.isVisible) {
                replacePixels(index, setLayerPixel(layer, row, col, color.toArgb(), blend))
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

    fun getAllPixels(): IntArray = getAllPixelsInRegion(0, 0, height, width)

    fun getCompositedPixelAt(row: Int, col: Int): Int {
        if (row !in 0 until height || col !in 0 until width) return transparentArgb
        return LayerCompositor.pixel(_layers) { tilePixelAt(it.tiles, row, col) }
    }

    fun getPreviewCompositedPixelAt(row: Int, col: Int, overlayColor: Int, blend: Boolean = true): Int {
        if (row !in 0 until height || col !in 0 until width) return transparentArgb
        return LayerCompositor.pixel(_layers) { layer ->
            val original = tilePixelAt(layer.tiles, row, col)
            if (layer.id != activeLayerId) original
            else if (blend) blendPixel(original, overlayColor, 1f) else overlayColor
        }
    }

    fun getAllPixelsInRegion(
        startRow: Int, startCol: Int,
        regionHeight: Int, regionWidth: Int
    ): IntArray {
        require(regionWidth >= 0 && regionHeight >= 0)
        val result = IntArray(Math.multiplyExact(regionWidth, regionHeight))
        LayerCompositor.render(_layers, width, height, result, startRow, startCol, regionWidth, regionHeight)
        return result
    }

    fun setAllPixels(pixels: IntArray, ignoreSelection: Boolean = false) {
        val idx = getActiveLayerIndex()
        if (idx !in _layers.indices) return
        val layer = _layers[idx]
        if (!layer.isLocked && pixels.size == width * height) {
            if (!ignoreSelection && selectionMask != null) {
                val currentPixels = tilesToPixels(layer.tiles, width, height)
                var changed = false
                for (i in pixels.indices) {
                    if (selectionMask!![i] && currentPixels[i] != pixels[i]) {
                        currentPixels[i] = pixels[i]
                        changed = true
                    }
                }
                if (changed) {
                    replacePixels(idx, layer.copy(tiles = pixelsToTiles(currentPixels, width, height)))
                }
            } else {
                replacePixels(idx, layer.copy(tiles = pixelsToTiles(pixels, width, height)))
            }
        }
    }

    fun setActiveLayerTiles(tiles: Map<TileCoord, IntArray>) {
        val index = getActiveLayerIndex()
        val layer = _layers.getOrNull(index) ?: return
        if (layer.isLocked || !layer.isVisible) return
        require(tiles.values.all { it.size == TILE_SIZE * TILE_SIZE })
        replacePixels(index, Layer(layer.id, layer.name, layer.isVisible, layer.isLocked,
            layer.opacity, layer.blendMode, tiles.toMap(), layer.isClipping))
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

        replacePixels(idx, layer.copy(tiles = updatedTiles))
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
        animation = Animation.fromLayers(_layers.toList())
        activeFrameId = animation.frames.first().id
    }

    fun getSprite(): Sprite {
        return Sprite(
            width = width,
            height = height,
            layers = _layers.toList(),
            animation = animation
        )
    }

    private fun replacePixels(index: Int, layer: Layer) {
        if (_layers[index] == layer) return
        val cel = animation.cel(layer.id, activeFrameId)
        if (cel == null && layer.tiles.isEmpty()) return
        val image = CelImage(cel?.dataId ?: UUID.randomUUID().toString(), layer.tiles)
        val cels = if (cel == null) animation.cels + AnimationCel(
            layerId = layer.id, frameId = activeFrameId, dataId = image.id
        ) else animation.cels
        animation = animation.copy(cels = cels, images = animation.images + (image.id to image))
        _layers[index] = layer
    }

    private fun resolveFrame() {
        val clippingLayerIds = animation.frames.first { it.id == activeFrameId }.clippingLayerIds
        for (i in _layers.indices) {
            val layer = _layers[i]
            _layers[i] = Layer(layer.id, layer.name, layer.isVisible, layer.isLocked,
                layer.opacity, layer.blendMode, animation.tiles(layer.id, activeFrameId), layer.id in clippingLayerIds)
        }
    }

    private fun prune(value: Animation): Animation {
        val referenced = value.cels.map { it.dataId }.toSet()
        return value.copy(images = value.images.filterKeys { it in referenced })
    }

    fun setSprite(sprite: Sprite) {
        setCanvasData(sprite.width, sprite.height, sprite.layers)
        animation = sprite.animation?.validated() ?: animation
        require(animation.cels.all { cel -> _layers.any { it.id == cel.layerId } })
        val layerIds = _layers.map { it.id }.toSet()
        require(animation.frames.all { layerIds.containsAll(it.clippingLayerIds) })
        activeFrameId = animation.frames.first().id
        resolveFrame()
    }

    fun selectFrame(id: String) {
        if (animation.frames.none { it.id == id }) return
        activeFrameId = id
        resolveFrame()
    }

    fun insertFrame(empty: Boolean = false, linked: Boolean = false) {
        if (animation.frames.size >= Animation.MAX_FRAMES) return
        val index = animation.frames.indexOfFirst { it.id == activeFrameId }
        val sourceFrame = animation.frames[index]
        val frame = AnimationFrame(durationMs = sourceFrame.durationMs,
            clippingLayerIds = if (empty) emptySet() else sourceFrame.clippingLayerIds)
        val frames = animation.frames.toMutableList().apply { add(index + 1, frame) }
        val images = animation.images.toMutableMap()
        val cels = animation.cels.toMutableList()
        if (!empty) {
            for (cel in animation.cels.filter { it.frameId == activeFrameId }) {
                val dataId = if (linked) cel.dataId else {
                    val image = CelImage(tiles = animation.images.getValue(cel.dataId).tiles)
                    images[image.id] = image
                    image.id
                }
                cels.add(cel.copy(id = UUID.randomUUID().toString(), frameId = frame.id, dataId = dataId))
            }
        }
        animation = animation.copy(frames = frames, cels = cels, images = images)
        selectFrame(frame.id)
    }

    fun deleteFrame() {
        if (animation.frames.size == 1) return
        val index = animation.frames.indexOfFirst { it.id == activeFrameId }
        val frames = animation.frames.filterNot { it.id == activeFrameId }
        animation = prune(animation.copy(frames = frames,
            cels = animation.cels.filterNot { it.frameId == activeFrameId },
            posterFrameId = animation.posterFrameId.takeIf { it != activeFrameId } ?: frames.first().id))
        selectFrame(frames[index.coerceAtMost(frames.lastIndex)].id)
    }

    fun setFrameDuration(durationMs: Int) {
        if (durationMs !in 1..60_000) return
        animation = animation.copy(frames = animation.frames.map {
            if (it.id == activeFrameId) it.copy(durationMs = durationMs) else it
        })
    }

    fun setPosterFrame() {
        animation = animation.copy(posterFrameId = activeFrameId)
    }

    fun clearCel() {
        if (_layers.firstOrNull { it.id == activeLayerId }?.isLocked != false) return
        animation = prune(animation.copy(cels = animation.cels.filterNot {
            it.layerId == activeLayerId && it.frameId == activeFrameId
        }))
        resolveFrame()
    }

    fun copyPreviousCel(linked: Boolean) {
        if (_layers.firstOrNull { it.id == activeLayerId }?.isLocked != false) return
        val index = animation.frames.indexOfFirst { it.id == activeFrameId }
        if (index <= 0) return
        val previous = animation.cel(activeLayerId, animation.frames[index - 1].id) ?: return
        val image = animation.images.getValue(previous.dataId)
        val replacement = if (linked) image else CelImage(tiles = image.tiles)
        val cels = animation.cels.filterNot { it.layerId == activeLayerId && it.frameId == activeFrameId } +
            AnimationCel(layerId = activeLayerId, frameId = activeFrameId, dataId = replacement.id)
        animation = prune(animation.copy(cels = cels, images = animation.images + (replacement.id to replacement)))
        resolveFrame()
    }

    fun unlinkCel() {
        if (_layers.firstOrNull { it.id == activeLayerId }?.isLocked != false) return
        val cel = animation.cel(activeLayerId, activeFrameId) ?: return
        if (animation.cels.count { it.dataId == cel.dataId } < 2) return
        val image = CelImage(tiles = animation.images.getValue(cel.dataId).tiles)
        animation = animation.copy(cels = animation.cels.map {
            if (it.id == cel.id) it.copy(dataId = image.id) else it
        }, images = animation.images + (image.id to image))
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
