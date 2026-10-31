package com.instasprite.app.utils

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class SpriteReadBudget(maxBytes: Long = minOf(128L * 1024 * 1024, Runtime.getRuntime().maxMemory() / 4)) {
    private var remaining = maxBytes

    init { require(maxBytes > 0) }

    fun consume(bytes: Long) {
        if (bytes < 0 || bytes > remaining) throw IOException("Sprite exceeds import memory limit")
        remaining -= bytes
    }

    fun limit(input: InputStream): InputStream = object : FilterInputStream(input) {
        override fun read(): Int {
            val value = `in`.read()
            if (value != -1) consume(1)
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            val count = `in`.read(buffer, offset, minOf(length.toLong(), remaining.coerceAtMost(Int.MAX_VALUE.toLong() - 1) + 1).toInt())
            if (count > 0) consume(count.toLong())
            return count
        }

        override fun skip(count: Long): Long {
            if (count <= 0) return 0
            val buffer = ByteArray(minOf(count, 8192).toInt())
            var skipped = 0L
            while (skipped < count) {
                val read = read(buffer, 0, minOf(buffer.size.toLong(), count - skipped).toInt())
                if (read == -1) break
                skipped += read
            }
            return skipped
        }

        override fun available(): Int = minOf(`in`.available().toLong(), remaining).toInt()
        override fun markSupported(): Boolean = false
        override fun reset() { throw IOException("Import stream cannot reset") }
    }
}
