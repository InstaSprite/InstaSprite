package com.instasprite.app.utils

class CanvasMemoryException : RuntimeException()

object CanvasMemoryGuard {
    fun requirePixels(width: Int, height: Int, bytesPerPixel: Long = 24) {
        requireAvailable(estimatePixels(width, height, bytesPerPixel))
    }

    internal fun estimatePixels(width: Int, height: Int, bytesPerPixel: Long): Long {
        val count = width.toLong() * height
        if (width <= 0 || height <= 0 || count > Int.MAX_VALUE || bytesPerPixel <= 0 ||
            bytesPerPixel > Long.MAX_VALUE / count) return Long.MAX_VALUE
        return count * bytesPerPixel
    }

    fun requireAvailable(vararg estimates: Long) {
        val required = estimates.fold(0L) { total, bytes ->
            if (bytes < 0 || bytes > Long.MAX_VALUE - total) Long.MAX_VALUE else total + bytes
        }
        val runtime = Runtime.getRuntime()
        checkBudget(required, runtime.maxMemory(), runtime.totalMemory() - runtime.freeMemory())
    }

    internal fun checkBudget(required: Long, maxHeap: Long, usedHeap: Long) {
        val available = minOf(maxHeap / 2, maxHeap - usedHeap - maxHeap / 4)
        if (required < 0 || required > available) throw CanvasMemoryException()
    }

    fun requireExport(width: Int, height: Int, scalePercent: Int): Pair<Int, Int> {
        if (width <= 0 || height <= 0 || scalePercent <= 0) throw CanvasMemoryException()
        val scaledWidth = (width.toLong() * scalePercent / 100).coerceAtLeast(1)
        val scaledHeight = (height.toLong() * scalePercent / 100).coerceAtLeast(1)
        if (scaledWidth > Int.MAX_VALUE || scaledHeight > Int.MAX_VALUE) throw CanvasMemoryException()
        requireAvailable(estimatePixels(width, height, 8),
            estimatePixels(scaledWidth.toInt(), scaledHeight.toInt(), 8))
        return scaledWidth.toInt() to scaledHeight.toInt()
    }
}
