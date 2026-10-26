package com.instasprite.app.domain.export

import com.instasprite.app.utils.CanvasMemoryException

object ExportScale {
    fun size(width: Int, height: Int, percent: Int): Pair<Int, Int> {
        require(width > 0 && height > 0 && percent in 25..20000)
        val w = (width.toLong() * percent / 100).coerceAtLeast(1)
        val h = (height.toLong() * percent / 100).coerceAtLeast(1)
        if (w > Int.MAX_VALUE || h > Int.MAX_VALUE) throw CanvasMemoryException()
        return w.toInt() to h.toInt()
    }

    fun sourceIndex(index: Int, sourceSize: Int, targetSize: Int): Int =
        ((2L * index + 1) * sourceSize / (2L * targetSize)).toInt()

    fun copy(pixels: IntArray, width: Int, height: Int, target: IntArray, targetWidth: Int, targetHeight: Int) {
        require(width > 0 && height > 0 && targetWidth > 0 && targetHeight > 0)
        require(pixels.size.toLong() == width.toLong() * height && target.size.toLong() == targetWidth.toLong() * targetHeight)
        for (row in 0 until targetHeight) {
            val sourceRow = sourceIndex(row, height, targetHeight) * width
            for (col in 0 until targetWidth) {
                target[row * targetWidth + col] = pixels[sourceRow + sourceIndex(col, width, targetWidth)]
            }
        }
    }
}
