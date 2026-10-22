package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.domain.model.LayerCompositor

class MoveCompositor(
    private val width: Int,
    private val height: Int,
    private val layers: List<Layer>,
    private val activeLayerId: String,
    val selectionMask: BooleanArray?
) {
    private val originalTiles = layers.first { it.id == activeLayerId }.tiles
    private val clippingScratch = if (layers.any { it.isClipping }) IntArray(TILE_SIZE * TILE_SIZE) else null
    private val selectedTiles = mutableMapOf<TileCoord, IntArray>()
    private val clearedTiles = originalTiles.toMutableMap()
    private var destinationTiles: Set<TileCoord> = emptySet()
    private var maskLeft = width
    private var maskTop = height
    private var maskRight = 0
    private var maskBottom = 0
    var tiles: Map<TileCoord, IntArray> = originalTiles
        private set

    init {
        require(width > 0 && height > 0)
        require(selectionMask == null || selectionMask.size.toLong() == width.toLong() * height)
        selectionMask?.forEachIndexed { index, selected ->
            if (selected) {
                val x = index % width
                val y = index / width
                maskLeft = minOf(maskLeft, x)
                maskTop = minOf(maskTop, y)
                maskRight = maxOf(maskRight, x + 1)
                maskBottom = maxOf(maskBottom, y + 1)
            }
        }
        for ((coord, pixels) in originalTiles) {
            var selected: IntArray? = null
            var remaining: IntArray? = null
            for (index in pixels.indices) {
                val x = coord.x.toLong() * TILE_SIZE + index % TILE_SIZE
                val y = coord.y.toLong() * TILE_SIZE + index / TILE_SIZE
                if (x !in 0L until width.toLong() || y !in 0L until height.toLong()) continue
                if (pixels[index] == 0 || selectionMask?.get(y.toInt() * width + x.toInt()) == false) continue
                if (selected == null) selected = IntArray(TILE_SIZE * TILE_SIZE)
                if (remaining == null) remaining = pixels.copyOf()
                selected[index] = pixels[index]
                remaining[index] = 0
            }
            if (selected != null) {
                selectedTiles[coord] = selected
                if (remaining!!.all { it == 0 }) clearedTiles.remove(coord)
                else clearedTiles[coord] = remaining
            }
        }
    }

    fun composite(offsetX: Int, offsetY: Int): List<Cel> {
        val moved = clearedTiles.toMutableMap()
        val destinations = mutableSetOf<TileCoord>()
        for ((coord, pixels) in selectedTiles) {
            for (index in pixels.indices) {
                val pixel = pixels[index]
                if (pixel == 0) continue
                val x = coord.x.toLong() * TILE_SIZE + index % TILE_SIZE + offsetX
                val y = coord.y.toLong() * TILE_SIZE + index / TILE_SIZE + offsetY
                if (x !in 0L until width.toLong() || y !in 0L until height.toLong()) continue
                val destination = TileCoord(x.toInt() / TILE_SIZE, y.toInt() / TILE_SIZE)
                if (destinations.add(destination)) {
                    moved[destination] = moved[destination]?.copyOf() ?: IntArray(TILE_SIZE * TILE_SIZE)
                }
                moved.getValue(destination)[(y.toInt() % TILE_SIZE) * TILE_SIZE + x.toInt() % TILE_SIZE] = pixel
            }
        }
        tiles = moved
        val affected = selectedTiles.keys + destinationTiles + destinations
        destinationTiles = destinations
        val previewLayers = layers.map { layer ->
            if (layer.id != activeLayerId) layer else Layer(layer.id, layer.name, layer.isVisible,
                layer.isLocked, layer.opacity, layer.blendMode, tiles, layer.isClipping)
        }
        return affected.map { coord ->
            val x = coord.x * TILE_SIZE
            val y = coord.y * TILE_SIZE
            val tileWidth = minOf(TILE_SIZE, width - x)
            val tileHeight = minOf(TILE_SIZE, height - y)
            val pixels = IntArray(tileWidth * tileHeight)
            LayerCompositor.render(previewLayers, width, height, pixels, y, x, tileWidth, tileHeight, clippingScratch)
            Cel(x, y, tileWidth, tileHeight, pixels)
        }
    }

    fun shiftedSelectionMask(offsetX: Int, offsetY: Int): BooleanArray? {
        val mask = selectionMask ?: return null
        val shifted = BooleanArray(mask.size)
        for (y in maskTop until maskBottom) {
            for (x in maskLeft until maskRight) {
                if (!mask[y * width + x]) continue
                val destinationX = x.toLong() + offsetX
                val destinationY = y.toLong() + offsetY
                if (destinationX in 0L until width.toLong() && destinationY in 0L until height.toLong()) {
                    shifted[destinationY.toInt() * width + destinationX.toInt()] = true
                }
            }
        }
        return shifted
    }
}
