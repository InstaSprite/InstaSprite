package com.olaz.instasprite.domain.canvashistory

import com.olaz.instasprite.domain.model.TileCoord
import java.util.LinkedHashMap

class TileChangeTracker {
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

    fun buildUndoEntry(): UndoEntry {
        if (capturedTiles.isEmpty()) return UndoEntry(emptyList())

        val deltas = capturedTiles.map { (key, tile) ->
            TileDelta(
                layerId = key.layerId,
                coord = key.coord,
                before = tile
            )
        }
        clear()
        return UndoEntry(deltas)
    }

    fun clear() {
        capturedTiles.clear()
    }

    fun isEmpty(): Boolean = capturedTiles.isEmpty()
}


