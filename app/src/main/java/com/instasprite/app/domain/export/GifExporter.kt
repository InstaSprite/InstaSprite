package com.instasprite.app.domain.export

import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.CanvasMemoryGuard
import java.io.OutputStream

object GifExporter {
    fun write(sprite: Sprite, output: OutputStream, scalePercent: Int = 100, checkCancelled: () -> Unit = {}) {
        val (width, height) = ExportScale.size(sprite.width, sprite.height, scalePercent)
        require(width in 1..65535 && height in 1..65535)
        val resized = width != sprite.width || height != sprite.height
        CanvasMemoryGuard.requireAvailable(
            CanvasMemoryGuard.estimatePixels(sprite.width, sprite.height, 4),
            CanvasMemoryGuard.estimatePixels(width, height, if (resized) 5 else 1)
        )
        val frames = requireNotNull(sprite.animation).validated().frames
        output.write("GIF89a".toByteArray(Charsets.US_ASCII))
        output.word(width)
        output.word(height)
        output.write(byteArrayOf(0xf0.toByte(), 0, 0, 0, 0, 0, 0, 0, 0))
        output.write(byteArrayOf(0x21, 0xff.toByte(), 11))
        output.write("NETSCAPE2.0".toByteArray(Charsets.US_ASCII))
        output.write(byteArrayOf(3, 1, 0, 0, 0))
        val source = IntArray(Math.multiplyExact(sprite.width, sprite.height))
        val pixels = if (resized) IntArray(Math.multiplyExact(width, height)) else source
        val indices = ByteArray(pixels.size)
        for (frame in frames) {
            checkCancelled()
            sprite.renderFrame(frame.id, source)
            if (resized) ExportScale.copy(source, sprite.width, sprite.height, pixels, width, height)
            val palette = indexPixels(pixels, indices)
            output.write(byteArrayOf(0x21, 0xf9.toByte(), 4, 9))
            output.word(((frame.durationMs + 5) / 10).coerceAtLeast(1))
            output.write(byteArrayOf(0, 0, 0x2c))
            output.word(0)
            output.word(0)
            output.word(width)
            output.word(height)
            output.write(0x87)
            for (color in palette) {
                output.write(color ushr 16 and 255)
                output.write(color ushr 8 and 255)
                output.write(color and 255)
            }
            output.write(8)
            writeLzw(indices, output, checkCancelled)
            output.write(0)
        }
        output.write(0x3b)
    }

    internal fun applyPreviewPalette(pixels: IntArray) {
        val indices = ByteArray(pixels.size)
        val palette = indexPixels(pixels, indices)
        for (i in pixels.indices) {
            val index = indices[i].toInt() and 255
            pixels[i] = if (index == 0) 0 else palette[index] or (0xff shl 24)
        }
    }

    private fun indexPixels(pixels: IntArray, indices: ByteArray): IntArray {
        val colors = LinkedHashMap<Int, Int>()
        var exact = true
        for (pixel in pixels) {
            if (pixel ushr 24 < 128) continue
            val rgb = pixel and 0xffffff
            if (rgb !in colors) {
                if (colors.size == 255) { exact = false; break }
                colors[rgb] = colors.size + 1
            }
        }
        val palette = IntArray(256)
        if (exact) {
            for ((rgb, index) in colors) palette[index] = rgb
        } else {
            for (r in 0..5) for (g in 0..5) for (b in 0..5) {
                palette[1 + r * 36 + g * 6 + b] = (r * 51 shl 16) or (g * 51 shl 8) or (b * 51)
            }
        }
        for (i in pixels.indices) {
            val pixel = pixels[i]
            indices[i] = when {
                pixel ushr 24 < 128 -> 0
                exact -> colors.getValue(pixel and 0xffffff)
                else -> 1 + ((pixel ushr 16 and 255) * 5 + 127) / 255 * 36 +
                    ((pixel ushr 8 and 255) * 5 + 127) / 255 * 6 + ((pixel and 255) * 5 + 127) / 255
            }.toByte()
        }
        return palette
    }

    private fun writeLzw(indices: ByteArray, output: OutputStream, checkCancelled: () -> Unit) {
        val dictionary = HashMap<Int, Int>(4096)
        val block = ByteArray(255)
        var blockSize = 0
        var buffer = 0
        var bufferedBits = 0
        var codeBits = 9
        var nextCode = 258
        fun byte(value: Int) {
            block[blockSize++] = value.toByte()
            if (blockSize == block.size) {
                output.write(blockSize)
                output.write(block)
                blockSize = 0
            }
        }
        fun code(value: Int) {
            buffer = buffer or (value shl bufferedBits)
            bufferedBits += codeBits
            while (bufferedBits >= 8) {
                byte(buffer and 255)
                buffer = buffer ushr 8
                bufferedBits -= 8
            }
        }
        code(256)
        var prefix = indices[0].toInt() and 255
        for (i in 1 until indices.size) {
            if (i and 4095 == 0) checkCancelled()
            val symbol = indices[i].toInt() and 255
            val key = prefix shl 8 or symbol
            val found = dictionary[key]
            if (found != null) {
                prefix = found
            } else {
                code(prefix)
                if (nextCode < 4096) {
                    dictionary[key] = nextCode++
                    if (nextCode > 1 shl codeBits && codeBits < 12) codeBits++
                } else {
                    code(256)
                    dictionary.clear()
                    nextCode = 258
                    codeBits = 9
                }
                prefix = symbol
            }
        }
        code(prefix)
        if (nextCode == 1 shl codeBits && codeBits < 12) codeBits++
        code(257)
        if (bufferedBits > 0) byte(buffer and 255)
        if (blockSize > 0) {
            output.write(blockSize)
            output.write(block, 0, blockSize)
        }
    }

    private fun OutputStream.word(value: Int) {
        write(value and 255)
        write(value ushr 8 and 255)
    }
}
