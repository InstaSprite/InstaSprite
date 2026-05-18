package com.instasprite.app.domain.canvashistory

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.pixelsToTiles
import com.instasprite.app.utils.tilesToPixels
import java.util.ArrayDeque
import java.util.LinkedHashMap

class CanvasHistoryManager {
    private val undoStack: ArrayDeque<HistoryEntry> = ArrayDeque()
    private val redoStack: ArrayDeque<HistoryEntry> = ArrayDeque()

    private val transparentTile = IntArray(TILE_SIZE * TILE_SIZE)

    fun push(entry: HistoryEntry) {
        if (entry.isEmpty()) return

        undoStack.addLast(entry)
        redoStack.clear()
    }

    fun undo(current: HistoryCanvasState): HistoryCanvasState? {
        val entry = if (undoStack.isNotEmpty()) undoStack.removeLast() else return null
        return when (entry) {
            is UndoEntry -> {
                val (restoredState, redoEntry) = applyTileEntry(entry, current)
                if (!redoEntry.isEmpty()) {
                    redoStack.addLast(redoEntry)
                }
                restoredState
            }
            is OperationEntry -> {
                redoStack.addLast(entry)
                deepCopyState(entry.before)
            }
            is TransformEntry -> {
                val transformed = applyTransform(current, inverseTransform(entry.transform))
                redoStack.addLast(entry)
                transformed
            }
        }
    }

    fun redo(current: HistoryCanvasState): HistoryCanvasState? {
        val entry = if (redoStack.isNotEmpty()) redoStack.removeLast() else return null
        return when (entry) {
            is UndoEntry -> {
                val (restoredState, undoEntry) = applyTileEntry(entry, current)
                if (!undoEntry.isEmpty()) {
                    undoStack.addLast(undoEntry)
                }
                restoredState
            }
            is OperationEntry -> {
                undoStack.addLast(entry)
                deepCopyState(entry.after)
            }
            is TransformEntry -> {
                val transformed = applyTransform(current, entry.transform)
                undoStack.addLast(entry)
                transformed
            }
        }
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
    }

    internal fun restore(entry: UndoEntry, current: HistoryCanvasState): HistoryCanvasState {
        return applyTileEntry(entry, current).first
    }

    private fun applyTileEntry(entry: UndoEntry, current: HistoryCanvasState): Pair<HistoryCanvasState, UndoEntry> {
        val layers = current.layers.toMutableList()
        val layerIndexById = HashMap<String, Int>(layers.size)
        for (i in layers.indices) {
            layerIndexById[layers[i].id] = i
        }

        val redoDeltas = ArrayList<TileDelta>(entry.deltas.size)
        val pendingLayerTiles = LinkedHashMap<String, MutableMap<TileCoord, IntArray>>()
        val originalLayers = LinkedHashMap<String, Layer>()

        for (delta in entry.deltas) {
            val layerIndex = layerIndexById[delta.layerId] ?: continue
            val layer = layers[layerIndex]
            originalLayers.putIfAbsent(delta.layerId, layer)

            val mutableTiles = pendingLayerTiles.getOrPut(delta.layerId) {
                LinkedHashMap(layer.tiles)
            }

            val currentTile = mutableTiles[delta.coord] ?: transparentTile
            redoDeltas.add(
                TileDelta(
                    layerId = delta.layerId,
                    coord = delta.coord,
                    before = currentTile.copyOf()
                )
            )

            if (isEmptyTile(delta.before)) {
                mutableTiles.remove(delta.coord)
            } else {
                mutableTiles[delta.coord] = delta.before.copyOf()
            }
        }

        for ((layerId, updatedTiles) in pendingLayerTiles) {
            val layerIndex = layerIndexById[layerId] ?: continue
            val original = originalLayers[layerId] ?: continue
            layers[layerIndex] = original.copy(tiles = updatedTiles)
        }

        val next = HistoryCanvasState(
            width = current.width,
            height = current.height,
            layers = layers,
            activeLayerId = current.activeLayerId
        )

        return Pair(next, UndoEntry(redoDeltas))
    }

    private fun deepCopyState(state: HistoryCanvasState): HistoryCanvasState {
        return HistoryCanvasState(
            width = state.width,
            height = state.height,
            layers = state.layers.map { it.copy() },
            activeLayerId = state.activeLayerId,
            selectionState = state.selectionState?.deepCopy()
        )
    }

    private fun inverseTransform(transform: TransformType): TransformType {
        return when (transform) {
            TransformType.ROTATE_CW -> TransformType.ROTATE_CCW
            TransformType.ROTATE_CCW -> TransformType.ROTATE_CW
            TransformType.FLIP_H -> TransformType.FLIP_H
            TransformType.FLIP_V -> TransformType.FLIP_V
        }
    }

    private fun applyTransform(state: HistoryCanvasState, transform: TransformType): HistoryCanvasState {
        return when (transform) {
            TransformType.ROTATE_CW -> rotateClockwise(state)
            TransformType.ROTATE_CCW -> rotateCounterClockwise(state)
            TransformType.FLIP_H -> horizontalFlip(state)
            TransformType.FLIP_V -> verticalFlip(state)
        }
    }

    private fun rotateClockwise(state: HistoryCanvasState): HistoryCanvasState {
        val oldWidth = state.width
        val oldHeight = state.height
        val newWidth = oldHeight
        val newHeight = oldWidth

        val rotatedLayers = state.layers.map { layer ->
            val source = tilesToPixels(layer.tiles, oldWidth, oldHeight)
            val rotated = IntArray(newWidth * newHeight)
            for (row in 0 until oldHeight) {
                val srcBase = row * oldWidth
                for (col in 0 until oldWidth) {
                    val argb = source[srcBase + col]
                    if (argb == 0) continue
                    val newRow = col
                    val newCol = oldHeight - 1 - row
                    rotated[newRow * newWidth + newCol] = argb
                }
            }
            layer.copy(tiles = pixelsToTiles(rotated, newWidth, newHeight))
        }

        return HistoryCanvasState(
            width = newWidth,
            height = newHeight,
            layers = rotatedLayers,
            activeLayerId = state.activeLayerId
        )
    }

    private fun rotateCounterClockwise(state: HistoryCanvasState): HistoryCanvasState {
        val oldWidth = state.width
        val oldHeight = state.height
        val newWidth = oldHeight
        val newHeight = oldWidth

        val rotatedLayers = state.layers.map { layer ->
            val source = tilesToPixels(layer.tiles, oldWidth, oldHeight)
            val rotated = IntArray(newWidth * newHeight)
            for (row in 0 until oldHeight) {
                val srcBase = row * oldWidth
                for (col in 0 until oldWidth) {
                    val argb = source[srcBase + col]
                    if (argb == 0) continue
                    val newRow = oldWidth - 1 - col
                    val newCol = row
                    rotated[newRow * newWidth + newCol] = argb
                }
            }
            layer.copy(tiles = pixelsToTiles(rotated, newWidth, newHeight))
        }

        return HistoryCanvasState(
            width = newWidth,
            height = newHeight,
            layers = rotatedLayers,
            activeLayerId = state.activeLayerId
        )
    }

    private fun horizontalFlip(state: HistoryCanvasState): HistoryCanvasState {
        val width = state.width
        val height = state.height
        val flippedLayers = state.layers.map { layer ->
            val source = tilesToPixels(layer.tiles, width, height)
            val flipped = IntArray(width * height)
            for (row in 0 until height) {
                val srcBase = row * width
                val dstBase = row * width
                for (col in 0 until width) {
                    val argb = source[srcBase + col]
                    if (argb == 0) continue
                    flipped[dstBase + (width - 1 - col)] = argb
                }
            }
            layer.copy(tiles = pixelsToTiles(flipped, width, height))
        }

        return HistoryCanvasState(
            width = width,
            height = height,
            layers = flippedLayers,
            activeLayerId = state.activeLayerId
        )
    }

    private fun verticalFlip(state: HistoryCanvasState): HistoryCanvasState {
        val width = state.width
        val height = state.height
        val flippedLayers = state.layers.map { layer ->
            val source = tilesToPixels(layer.tiles, width, height)
            val flipped = IntArray(width * height)
            for (row in 0 until height) {
                val srcBase = row * width
                for (col in 0 until width) {
                    val argb = source[srcBase + col]
                    if (argb == 0) continue
                    flipped[(height - 1 - row) * width + col] = argb
                }
            }
            layer.copy(tiles = pixelsToTiles(flipped, width, height))
        }

        return HistoryCanvasState(
            width = width,
            height = height,
            layers = flippedLayers,
            activeLayerId = state.activeLayerId
        )
    }

    private fun isEmptyTile(tile: IntArray): Boolean {
        return tile.all { it == 0 }
    }
}
