package com.instasprite.app.domain.canvashistory

import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.pixelsToTiles
import com.instasprite.app.utils.tilesToPixels
import java.util.ArrayDeque

class CanvasHistoryManager(
    private val diskStore: HistoryDiskStore? = null,
    private val memoryBudgetBytes: Long = minOf(
        32L * 1024 * 1024,
        Runtime.getRuntime().maxMemory() / 8
    )
) {
    private val undoStack: ArrayDeque<HistoryEntry> = ArrayDeque()
    private val redoStack: ArrayDeque<HistoryEntry> = ArrayDeque()
    private val memoryUsage = HistoryMemoryUsage()

    internal val retainedMemoryBytes: Long get() = memoryUsage.bytes

    private val transparentTile = IntArray(TILE_SIZE * TILE_SIZE)

    init {
        require(memoryBudgetBytes > 0)
    }

    fun push(entry: HistoryEntry) {
        if (entry.isEmpty()) return
        pushUndo(entry)
        while (redoStack.isNotEmpty()) memoryUsage.release(redoStack.removeLast())
        diskStore?.clearRedo()
        trimHistory()
    }

    fun undo(current: HistoryCanvasState): HistoryCanvasState? {
        val entry = undoStack.pollLast() ?: diskStore?.popUndo()?.also { memoryUsage.retain(it) }
        ?: return null

        return try {
            when (entry) {
                is CelEditEntry -> {
                    pushRedo(entry)
                    applyCelEdit(entry, current, true)
                }

                is UndoEntry -> {
                    val (restoredState, redoEntry) = applyTileEntry(entry, current)
                    if (!redoEntry.isEmpty()) {
                        pushRedo(redoEntry)
                    }
                    restoredState
                }

                is OperationEntry -> {
                    pushRedo(entry)
                    deepCopyState(entry.before)
                }

                is TransformEntry -> {
                    val transformed = applyTransform(current, inverseTransform(entry.transform))
                    pushRedo(entry)
                    transformed
                }
            }
        } finally {
            memoryUsage.release(entry)
            trimHistory()
        }
    }

    fun redo(current: HistoryCanvasState): HistoryCanvasState? {
        val entry = redoStack.pollLast() ?: diskStore?.popRedo()?.also { memoryUsage.retain(it) }
        ?: return null

        return try {
            when (entry) {
                is CelEditEntry -> {
                    pushUndo(entry)
                    applyCelEdit(entry, current, false)
                }

                is UndoEntry -> {
                    val (restoredState, undoEntry) = applyTileEntry(entry, current)
                    if (!undoEntry.isEmpty()) {
                        pushUndo(undoEntry)
                    }
                    restoredState
                }

                is OperationEntry -> {
                    pushUndo(entry)
                    deepCopyState(entry.after)
                }

                is TransformEntry -> {
                    val transformed = applyTransform(current, entry.transform)
                    pushUndo(entry)
                    transformed
                }
            }
        } finally {
            memoryUsage.release(entry)
            trimHistory()
        }
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        memoryUsage.clear()
        diskStore?.clearAll()
    }

    val canUndo: Boolean
        get() = undoStack.isNotEmpty() || (diskStore?.undoCount ?: 0) > 0

    val canRedo: Boolean
        get() = redoStack.isNotEmpty() || (diskStore?.redoCount ?: 0) > 0

    internal fun restore(entry: UndoEntry, current: HistoryCanvasState): HistoryCanvasState {
        return applyTileEntry(entry, current).first
    }

    private fun pushUndo(entry: HistoryEntry) {
        undoStack.addLast(entry)
        memoryUsage.retain(entry)
    }

    private fun pushRedo(entry: HistoryEntry) {
        redoStack.addLast(entry)
        memoryUsage.retain(entry)
    }

    private fun trimHistory() {
        while (memoryUsage.bytes > memoryBudgetBytes) {
            val undo = undoStack.isNotEmpty()
            val stack = if (undo) undoStack else redoStack
            val entry = stack.peekFirst() ?: return
            if (diskStore != null) {
                val stored = if (undo) diskStore.appendUndo(entry) else diskStore.appendRedo(entry)
                if (!stored) return
            }
            stack.removeFirst()
            memoryUsage.release(entry)
        }
    }

    private fun applyTileEntry(
        entry: UndoEntry,
        current: HistoryCanvasState
    ): Pair<HistoryCanvasState, UndoEntry> {
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

    private fun applyCelEdit(
        entry: CelEditEntry,
        state: HistoryCanvasState,
        undo: Boolean
    ): HistoryCanvasState {
        val sprite = requireNotNull(state.sprite)
        val animation = requireNotNull(sprite.animation)
        val cel = if (undo) entry.beforeCel else entry.afterCel
        val image = if (undo) entry.beforeImagePatch else entry.afterImagePatch
        val cels =
            animation.cels.filterNot { it.layerId == entry.layerId && it.frameId == entry.frameId } + listOfNotNull(
                cel
            )
        val images = animation.images.toMutableMap()
        image?.let {
            val tiles = images[it.id]?.tiles?.toMutableMap() ?: mutableMapOf()
            val touched = (entry.beforeImagePatch?.tiles?.keys ?: emptySet()) +
                    (entry.afterImagePatch?.tiles?.keys ?: emptySet())
            touched.forEach { coord -> tiles.remove(coord) }
            tiles.putAll(it.tiles)
            images[it.id] = CelImage(it.id, tiles)
        }
        val referenced = cels.map { it.dataId }.toSet()
        val nextAnimation =
            animation.copy(cels = cels, images = images.filterKeys { it in referenced })
        val nextSprite = sprite.copy(animation = nextAnimation)
        val selection = if (undo) entry.beforeSelection else entry.afterSelection
        return state.copy(
            sprite = nextSprite,
            layers = nextSprite.layersAt(entry.frameId),
            activeFrameId = entry.frameId,
            activeLayerId = entry.layerId,
            selectionState = selection?.deepCopy()
        )
    }

    private fun deepCopyState(state: HistoryCanvasState): HistoryCanvasState {
        return state.copy(selectionState = state.selectionState?.deepCopy())
    }

    private fun inverseTransform(transform: TransformType): TransformType {
        return when (transform) {
            TransformType.ROTATE_CW -> TransformType.ROTATE_CCW
            TransformType.ROTATE_CCW -> TransformType.ROTATE_CW
            TransformType.FLIP_H -> TransformType.FLIP_H
            TransformType.FLIP_V -> TransformType.FLIP_V
        }
    }

    private fun applyTransform(
        state: HistoryCanvasState,
        transform: TransformType
    ): HistoryCanvasState {
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
