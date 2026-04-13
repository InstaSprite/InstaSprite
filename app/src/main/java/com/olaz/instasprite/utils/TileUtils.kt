package com.olaz.instasprite.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.domain.model.Cel
import com.olaz.instasprite.domain.model.TileCoord
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

const val TILE_SIZE = 16
private val TRANSPARENT_ARGB = Color.Transparent.toArgb()
private const val TILE_STREAM_MAGIC = 0x54494C45 // 'TILE'
private const val TILE_STREAM_VERSION = 1

fun pixelToTileCoord(row: Int, col: Int): TileCoord {
    return TileCoord(col.floorDiv(TILE_SIZE), row.floorDiv(TILE_SIZE))
}

fun tileOrigin(coord: TileCoord): Pair<Int, Int> = Pair(coord.x * TILE_SIZE, coord.y * TILE_SIZE)

fun pixelsToTiles(pixels: IntArray, canvasWidth: Int, canvasHeight: Int): Map<TileCoord, IntArray> {
    if (canvasWidth <= 0 || canvasHeight <= 0 || pixels.size != canvasWidth * canvasHeight) return emptyMap()

    val result = LinkedHashMap<TileCoord, IntArray>()
    val tileCols = (canvasWidth + TILE_SIZE - 1) / TILE_SIZE
    val tileRows = (canvasHeight + TILE_SIZE - 1) / TILE_SIZE

    for (tileY in 0 until tileRows) {
        for (tileX in 0 until tileCols) {
            val tilePixels = IntArray(TILE_SIZE * TILE_SIZE)
            var hasOpaque = false
            for (localRow in 0 until TILE_SIZE) {
                val canvasRow = tileY * TILE_SIZE + localRow
                if (canvasRow >= canvasHeight) break
                val srcBase = canvasRow * canvasWidth
                val dstBase = localRow * TILE_SIZE
                for (localCol in 0 until TILE_SIZE) {
                    val canvasCol = tileX * TILE_SIZE + localCol
                    if (canvasCol >= canvasWidth) break
                    val argb = pixels[srcBase + canvasCol]
                    tilePixels[dstBase + localCol] = argb
                    if (argb != TRANSPARENT_ARGB) hasOpaque = true
                }
            }
            if (hasOpaque) {
                result[TileCoord(tileX, tileY)] = tilePixels
            }
        }
    }

    return result
}

fun tilesToPixels(tiles: Map<TileCoord, IntArray>, canvasWidth: Int, canvasHeight: Int): IntArray {
    val result = IntArray(canvasWidth * canvasHeight) { TRANSPARENT_ARGB }
    if (canvasWidth <= 0 || canvasHeight <= 0 || tiles.isEmpty()) return result

    for ((coord, tilePixels) in tiles) {
        val originX = coord.x * TILE_SIZE
        val originY = coord.y * TILE_SIZE
        for (localRow in 0 until TILE_SIZE) {
            val canvasRow = originY + localRow
            if (canvasRow !in 0 until canvasHeight) continue
            val srcBase = localRow * TILE_SIZE
            val dstBase = canvasRow * canvasWidth
            for (localCol in 0 until TILE_SIZE) {
                val canvasCol = originX + localCol
                if (canvasCol !in 0 until canvasWidth) continue
                val argb = tilePixels[srcBase + localCol]
                if (argb != TRANSPARENT_ARGB) {
                    result[dstBase + canvasCol] = argb
                }
            }
        }
    }

    return result
}

fun tilesToCel(tiles: Map<TileCoord, IntArray>): Cel {
    if (tiles.isEmpty()) return Cel(0, 0, 0, 0, IntArray(0))

    var minTileX = Int.MAX_VALUE
    var minTileY = Int.MAX_VALUE
    var maxTileX = Int.MIN_VALUE
    var maxTileY = Int.MIN_VALUE
    var hasOpaque = false

    for ((coord, tilePixels) in tiles) {
        if (tilePixels.any { it != TRANSPARENT_ARGB }) {
            hasOpaque = true
            if (coord.x < minTileX) minTileX = coord.x
            if (coord.y < minTileY) minTileY = coord.y
            if (coord.x > maxTileX) maxTileX = coord.x
            if (coord.y > maxTileY) maxTileY = coord.y
        }
    }

    if (!hasOpaque) return Cel(0, 0, 0, 0, IntArray(0))

    val left = minTileX * TILE_SIZE
    val top = minTileY * TILE_SIZE
    val right = (maxTileX + 1) * TILE_SIZE
    val bottom = (maxTileY + 1) * TILE_SIZE
    val width = right - left
    val height = bottom - top
    val result = IntArray(width * height)

    for ((coord, tilePixels) in tiles) {
        val originX = coord.x * TILE_SIZE - left
        val originY = coord.y * TILE_SIZE - top
        for (row in 0 until TILE_SIZE) {
            val dstBase = (originY + row) * width + originX
            val srcBase = row * TILE_SIZE
            System.arraycopy(tilePixels, srcBase, result, dstBase, TILE_SIZE)
        }
    }

    return Cel(left, top, width, height, result)
}

fun celToTiles(cel: Cel): Map<TileCoord, IntArray> {
    if (cel.width <= 0 || cel.height <= 0 || cel.pixels.isEmpty()) return emptyMap()
    val result = LinkedHashMap<TileCoord, IntArray>()

    for (row in 0 until cel.height) {
        for (col in 0 until cel.width) {
            val argb = cel.pixels[row * cel.width + col]
            if (argb == TRANSPARENT_ARGB) continue

            val absRow = cel.y + row
            val absCol = cel.x + col
            val coord = pixelToTileCoord(absRow, absCol)
            val tile = result[coord]?.copyOf() ?: IntArray(TILE_SIZE * TILE_SIZE)
            val localRow = absRow - coord.y * TILE_SIZE
            val localCol = absCol - coord.x * TILE_SIZE
            tile[localRow * TILE_SIZE + localCol] = argb
            result[coord] = tile
        }
    }

    return result.filterValues { !it.all { px -> px == TRANSPARENT_ARGB } }
}

fun encodeTilesToByteArray(tiles: Map<TileCoord, IntArray>): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteBuffer.allocate(4 + 4 + 4 + 4)
        .order(ByteOrder.LITTLE_ENDIAN)
    buffer.putInt(TILE_STREAM_MAGIC)
    buffer.putInt(TILE_STREAM_VERSION)
    buffer.putInt(TILE_SIZE)
    buffer.putInt(tiles.size)
    output.write(buffer.array())

    for ((coord, tilePixels) in tiles) {
        val runs = encodeRle(tilePixels)
        val tileBuffer = ByteBuffer.allocate(4 * 5 + 4 + runs.size * 8).order(ByteOrder.LITTLE_ENDIAN)
        tileBuffer.putInt(coord.x)
        tileBuffer.putInt(coord.y)
        tileBuffer.putInt(TILE_SIZE)
        tileBuffer.putInt(TILE_SIZE)
        tileBuffer.putInt(tilePixels.size)
        tileBuffer.putInt(runs.size)
        for ((len, px) in runs) {
            tileBuffer.putInt(len)
            tileBuffer.putInt(px)
        }
        output.write(tileBuffer.array())
    }

    return gzip(output.toByteArray())
}

fun decodeTilesFromByteArray(bytes: ByteArray): Map<TileCoord, IntArray>? {
    if (bytes.isEmpty()) return emptyMap()
    val raw = ungzip(bytes)
    if (raw.size < 16) return null
    val header = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
    val magic = header.int
    if (magic != TILE_STREAM_MAGIC) return null
    val version = header.int
    if (version != TILE_STREAM_VERSION) return null
    val tileSize = header.int
    if (tileSize != TILE_SIZE) return null
    val count = header.int
    val result = LinkedHashMap<TileCoord, IntArray>(count)

    repeat(count) {
        if (header.remaining() < 20) return null
        val tileX = header.int
        val tileY = header.int
        val tileWidth = header.int
        val tileHeight = header.int
        val pixelCount = header.int
        val runCount = header.int
        if (tileWidth != TILE_SIZE || tileHeight != TILE_SIZE || pixelCount != TILE_SIZE * TILE_SIZE) return null
        if (header.remaining() < runCount * 8) return null
        val pixels = decodeRle(header, pixelCount, runCount)
        result[TileCoord(tileX, tileY)] = pixels
    }

    return result
}

private fun encodeRle(pixels: IntArray): List<Pair<Int, Int>> {
    if (pixels.isEmpty()) return emptyList()
    val runs = ArrayList<Pair<Int, Int>>()
    var current = pixels[0]
    var length = 1
    for (i in 1 until pixels.size) {
        val value = pixels[i]
        if (value == current) {
            length++
        } else {
            runs.add(length to current)
            current = value
            length = 1
        }
    }
    runs.add(length to current)
    return runs
}

private fun decodeRle(buffer: ByteBuffer, pixelCount: Int, runCount: Int): IntArray {
    val pixels = IntArray(pixelCount)
    var offset = 0
    repeat(runCount) {
        val len = buffer.int
        val px = buffer.int
        val end = minOf(offset + len, pixelCount)
        for (i in offset until end) {
            pixels[i] = px
        }
        offset = end
    }
    return pixels
}

private fun gzip(input: ByteArray): ByteArray {
    val output = ByteArrayOutputStream(input.size)
    GZIPOutputStream(output).use { gzip ->
        gzip.write(input)
        gzip.finish()
    }
    return output.toByteArray()
}

private fun ungzip(input: ByteArray): ByteArray {
    ByteArrayInputStream(input).use { byteStream ->
        GZIPInputStream(byteStream).use { gzip ->
            return gzip.readBytes()
        }
    }
}



