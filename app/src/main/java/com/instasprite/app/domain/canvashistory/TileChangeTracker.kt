package com.instasprite.app.domain.canvashistory

import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.model.TileCoord

class TileChangeTracker(
    private val beforeSelection: SelectionState? = null
) {
    private data class Key(
        val layerId: String,
        val coord: TileCoord,
    )

    private val capturedTiles = LinkedHashMap<Key, IntArray>()

    fun captureIfNeeded(layerId: String, coord: TileCoord, originalTile: IntArray) {
        capturedTiles.putIfAbsent(
            Key(layerId, coord),
            originalTile.copyOf()
        )
    }

    fun buildUndoEntry(afterSelection: SelectionState? = null): UndoEntry {
        if (capturedTiles.isEmpty() && beforeSelection == afterSelection) return UndoEntry(
            emptyList(),
            beforeSelection,
            afterSelection
        )

        val deltas = capturedTiles.map { (key, tile) ->
            TileDelta(
                layerId = key.layerId,
                coord = key.coord,
                before = tile
            )
        }
        clear()
        return UndoEntry(deltas, beforeSelection, afterSelection)
    }

    fun clear() {
        capturedTiles.clear()
    }

    fun isEmpty(): Boolean = capturedTiles.isEmpty()
}


