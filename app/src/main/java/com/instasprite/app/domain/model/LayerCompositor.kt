package com.instasprite.app.domain.model

import com.instasprite.app.utils.TILE_SIZE
import com.instasprite.app.utils.blendPixel

object LayerCompositor {
    fun blendClipped(base: Int, source: Int, opacity: Float, mode: BlendMode): Int {
        if (base ushr 24 == 0) return 0
        val color = blendPixel(base or (0xff shl 24), source, opacity, mode)
        return (base and (0xff shl 24)) or (color and 0x00ffffff)
    }

    inline fun pixel(layers: List<Layer>, pixelAt: (Layer) -> Int): Int {
        var result = 0
        var base: Layer? = null
        var group = 0
        for (layer in layers) {
            if (!layer.isClipping) {
                base?.let { result = blendPixel(result, group, it.opacity, it.blendMode) }
                base = layer
                group = if (layer.isVisible) pixelAt(layer) else 0
            } else if (base != null && layer.isVisible && group ushr 24 != 0) {
                group = blendClipped(group, pixelAt(layer), layer.opacity, layer.blendMode)
            }
        }
        base?.let { result = blendPixel(result, group, it.opacity, it.blendMode) }
        return result
    }

    fun render(
        layers: List<Layer>, width: Int, height: Int,
        pixels: IntArray, startRow: Int = 0, startCol: Int = 0,
        regionWidth: Int = width, regionHeight: Int = height,
        clippingScratch: IntArray? = null
    ) {
        require(regionWidth >= 0 && regionHeight >= 0 && pixels.size.toLong() == regionWidth.toLong() * regionHeight)
        require(clippingScratch == null || (clippingScratch.size == TILE_SIZE * TILE_SIZE && clippingScratch !== pixels))
        pixels.fill(0)
        if (regionWidth == 0 || regionHeight == 0) return
        val firstRow = maxOf(0, startRow).floorDiv(TILE_SIZE)
        val firstCol = maxOf(0, startCol).floorDiv(TILE_SIZE)
        val lastRow = (minOf(
            height.toLong(),
            startRow.toLong() + regionHeight
        ) - 1).floorDiv(TILE_SIZE.toLong()).toInt()
        val lastCol = (minOf(
            width.toLong(),
            startCol.toLong() + regionWidth
        ) - 1).floorDiv(TILE_SIZE.toLong()).toInt()
        if (firstRow > lastRow || firstCol > lastCol) return
        val regionTiles = (lastRow - firstRow + 1).toLong() * (lastCol - firstCol + 1)
        var scratch = clippingScratch
        var index = 0
        while (index < layers.size) {
            val base = layers[index]
            var end = index + 1
            while (end < layers.size && layers[end].isClipping) end++
            if (!base.isClipping && base.isVisible && base.opacity > 0f) {
                fun drawTile(coord: TileCoord, source: IntArray) {
                    if (coord.x !in firstCol..lastCol || coord.y !in firstRow..lastRow) return
                    val tile = if (end == index + 1) source else {
                        val group = scratch ?: IntArray(TILE_SIZE * TILE_SIZE).also { scratch = it }
                        source.copyInto(group)
                        for (clippedIndex in index + 1 until end) {
                            val clipped = layers[clippedIndex]
                            if (!clipped.isVisible || clipped.opacity <= 0f) continue
                            val overlay = clipped.tiles[coord] ?: continue
                            for (i in group.indices) {
                                group[i] = blendClipped(
                                    group[i],
                                    overlay[i],
                                    clipped.opacity,
                                    clipped.blendMode
                                )
                            }
                        }
                        group
                    }
                    val originX = coord.x * TILE_SIZE
                    val originY = coord.y * TILE_SIZE
                    val top = maxOf(0, startRow, originY)
                    val bottom = minOf(
                        height.toLong(),
                        startRow.toLong() + regionHeight,
                        originY.toLong() + TILE_SIZE
                    ).toInt()
                    val left = maxOf(0, startCol, originX)
                    val right = minOf(
                        width.toLong(),
                        startCol.toLong() + regionWidth,
                        originX.toLong() + TILE_SIZE
                    ).toInt()
                    for (row in top until bottom) {
                        val dst = (row - startRow) * regionWidth
                        val src = (row - originY) * TILE_SIZE
                        for (col in left until right) {
                            val offset = dst + col - startCol
                            pixels[offset] = blendPixel(
                                pixels[offset],
                                tile[src + col - originX],
                                base.opacity,
                                base.blendMode
                            )
                        }
                    }
                }
                if (regionTiles < base.tiles.size) {
                    for (row in firstRow..lastRow) for (col in firstCol..lastCol) {
                        val coord = TileCoord(col, row)
                        base.tiles[coord]?.let { drawTile(coord, it) }
                    }
                } else {
                    for ((coord, tile) in base.tiles) drawTile(coord, tile)
                }
            }
            index = end
        }
    }
}

fun List<Layer>.canMergeLayerDown(id: String, animation: Animation? = null): Boolean {
    val index = indexOfFirst { it.id == id }
    if (index <= 0) return false
    if (animation != null) {
        val topClipping = id in animation.frames.first().clippingLayerIds
        return animation.frames.all { frame ->
            (id in frame.clippingLayerIds) == topClipping &&
                    this[index - 1].id !in frame.clippingLayerIds &&
                    (topClipping || getOrNull(index + 1)?.id !in frame.clippingLayerIds)
        }
    }
    return !this[index - 1].isClipping && (this[index].isClipping || getOrNull(index + 1)?.isClipping != true)
}
