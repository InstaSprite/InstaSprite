package com.instasprite.app.ui.drawing.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.instasprite.app.domain.tool.DrawingSymmetry
import kotlin.math.abs
import kotlin.math.roundToInt

internal class SymmetryAxisDrag private constructor(
    private val isXAxis: Boolean,
    private val start: Float,
    private val axis: Float,
    private val cellSize: Float,
    private val dimension: Int,
    private val onMove: (Boolean, Float) -> Unit
) {
    private var lastPosition = axis

    fun move(position: Offset) {
        val coordinate = if (isXAxis) position.x else position.y
        val next = ((axis + (coordinate - start) / cellSize).coerceIn(0f, dimension.toFloat()) * 2).roundToInt() / 2f
        if (next != lastPosition) {
            lastPosition = next
            onMove(isXAxis, next)
        }
    }

    companion object {
        fun start(symmetry: DrawingSymmetry, position: Offset, size: IntSize, width: Int, height: Int,
            tolerance: Float, onMove: (Boolean, Float) -> Unit): SymmetryAxisDrag? {
            if (size.width <= 0 || size.height <= 0 || width <= 0 || height <= 0) return null
            val cellWidth = size.width.toFloat() / width
            val cellHeight = size.height.toFloat() / height
            val radius = minOf(tolerance, size.width / 8f, size.height / 8f)
            val atHorizontalEdge = position.y <= radius * 2 || position.y >= size.height - radius * 2
            val atVerticalEdge = position.x <= radius * 2 || position.x >= size.width - radius * 2
            val dx = if (symmetry.horizontal && atHorizontalEdge) abs(position.x - symmetry.x(width) * cellWidth) else Float.POSITIVE_INFINITY
            val dy = if (symmetry.vertical && atVerticalEdge) abs(position.y - symmetry.y(height) * cellHeight) else Float.POSITIVE_INFINITY
            if (minOf(dx, dy) > radius) return null
            val isXAxis = dx <= dy
            return SymmetryAxisDrag(isXAxis, if (isXAxis) position.x else position.y,
                if (isXAxis) symmetry.x(width) else symmetry.y(height),
                if (isXAxis) cellWidth else cellHeight, if (isXAxis) width else height, onMove)
        }
    }
}
