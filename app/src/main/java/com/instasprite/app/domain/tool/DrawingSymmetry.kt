package com.instasprite.app.domain.tool

import kotlin.math.roundToInt

data class DrawingSymmetry(
    val horizontal: Boolean = false,
    val vertical: Boolean = false,
    val axisX: Float? = null,
    val axisY: Float? = null
) {
    fun x(width: Int): Float = axisX?.takeIf { it.isFinite() }?.coerceIn(0f, width.toFloat()) ?: width / 2f
    fun y(height: Int): Float = axisY?.takeIf { it.isFinite() }?.coerceIn(0f, height.toFloat()) ?: height / 2f

    inline fun forEachPixel(row: Int, col: Int, width: Int, height: Int, plot: (Int, Int) -> Unit) {
        if (row !in 0 until height || col !in 0 until width) return
        plot(row, col)
        val mirroredRow = (2 * y(height)).roundToInt() - 1 - row
        val mirroredCol = (2 * x(width)).roundToInt() - 1 - col
        val reflectX = horizontal && mirroredCol != col && mirroredCol in 0 until width
        val reflectY = vertical && mirroredRow != row && mirroredRow in 0 until height
        if (reflectX) plot(row, mirroredCol)
        if (reflectY) plot(mirroredRow, col)
        if (reflectX && reflectY) plot(mirroredRow, mirroredCol)
    }
}
