package com.olaz.instasprite.utils

import android.graphics.Rect
import com.olaz.instasprite.domain.model.Cel

fun calculateBoundingBox(
    pixels: IntArray,
    canvasWidth: Int,
    canvasHeight: Int
): Rect? {
    if (canvasWidth <= 0 || canvasHeight <= 0) return null
    if (pixels.size != canvasWidth * canvasHeight) return null

    var left = canvasWidth
    var top = canvasHeight
    var right = -1
    var bottom = -1

    for (index in pixels.indices) {
        val color = pixels[index]
        val alpha = color ushr 24
        if (alpha == 0) continue

        val y = index / canvasWidth
        val x = index % canvasWidth

        if (x < left) left = x
        if (x > right) right = x
        if (y < top) top = y
        if (y > bottom) bottom = y
    }

    if (right < left || bottom < top) return null
    return Rect(left, top, right + 1, bottom + 1)
}

fun extractCel(pixels: IntArray, bounds: Rect, canvasWidth: Int): Cel {
    val celWidth = bounds.width()
    val celHeight = bounds.height()
    val celPixels = IntArray(celWidth * celHeight)

    for (row in 0 until celHeight) {
        val sourceOffset = (bounds.top + row) * canvasWidth + bounds.left
        val targetOffset = row * celWidth
        System.arraycopy(pixels, sourceOffset, celPixels, targetOffset, celWidth)
    }

    return Cel(
        x = bounds.left,
        y = bounds.top,
        width = celWidth,
        height = celHeight,
        pixels = celPixels
    )
}

fun inflateCel(cel: Cel?, canvasWidth: Int, canvasHeight: Int): IntArray {
    val canvasPixels = IntArray(canvasWidth * canvasHeight)
    if (cel == null || cel.width == 0 || cel.height == 0) return canvasPixels

    val copyWidth = minOf(cel.width, canvasWidth - cel.x)
    val copyHeight = minOf(cel.height, canvasHeight - cel.y)
    if (copyWidth <= 0 || copyHeight <= 0) return canvasPixels

    for (row in 0 until copyHeight) {
        val sourceOffset = row * cel.width
        val targetOffset = (cel.y + row) * canvasWidth + cel.x
        System.arraycopy(cel.pixels, sourceOffset, canvasPixels, targetOffset, copyWidth)
    }

    return canvasPixels
}
