package com.olaz.instasprite.domain.model

import com.olaz.instasprite.utils.celToTiles
import com.olaz.instasprite.utils.tilesToCel

class Layer(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val tiles: Map<TileCoord, IntArray>
) {
    constructor(
        id: String,
        name: String,
        isVisible: Boolean = true,
        isLocked: Boolean = false,
        cel: Cel
    ) : this(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        tiles = celToTiles(cel)
    )

    val cel: Cel
        get() = tilesToCel(tiles)

    fun copy(
        id: String = this.id,
        name: String = this.name,
        isVisible: Boolean = this.isVisible,
        isLocked: Boolean = this.isLocked,
        tiles: Map<TileCoord, IntArray>? = null,
        cel: Cel? = null
    ): Layer {
        val nextTiles = when {
            tiles != null -> deepCopyTiles(tiles)
            cel != null -> celToTiles(cel)
            else -> deepCopyTiles(this.tiles)
        }
        return Layer(id, name, isVisible, isLocked, nextTiles)
    }

    private fun deepCopyTiles(source: Map<TileCoord, IntArray>): Map<TileCoord, IntArray> {
        if (source.isEmpty()) return emptyMap()
        return source.mapValues { it.value.copyOf() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Layer) return false
        if (id != other.id || name != other.name || isVisible != other.isVisible || isLocked != other.isLocked) return false
        if (tiles.size != other.tiles.size) return false
        for ((coord, pixels) in tiles) {
            val otherPixels = other.tiles[coord] ?: return false
            if (!pixels.contentEquals(otherPixels)) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + isLocked.hashCode()
        for ((coord, pixels) in tiles) {
            result = 31 * result + coord.hashCode()
            result = 31 * result + pixels.contentHashCode()
        }
        return result
    }

    override fun toString(): String =
        "Layer(id='$id', name='$name', isVisible=$isVisible, isLocked=$isLocked, tiles=${tiles.size}, cel=$cel)"
}
