package com.instasprite.app.ui.drawing.gesture

import androidx.compose.ui.geometry.Rect
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
            tolerance: Float, visibleBounds: Rect = Rect(0f, 0f, size.width.toFloat(), size.height.toFloat()),
            onMove: (Boolean, Float) -> Unit): SymmetryAxisDrag? {
            if (size.width <= 0 || size.height <= 0 || width <= 0 || height <= 0) return null
            if (visibleBounds.isEmpty) return null
            val cellWidth = size.width.toFloat() / width
            val cellHeight = size.height.toFloat() / height
            val radius = minOf(tolerance, visibleBounds.width / 8f, visibleBounds.height / 8f)
            val x = (symmetry.x(width) * cellWidth).coerceIn(visibleBounds.left + radius, visibleBounds.right - radius)
            val y = (symmetry.y(height) * cellHeight).coerceIn(visibleBounds.top + radius, visibleBounds.bottom - radius)
            val atHorizontalEdge = position.y in visibleBounds.top..(visibleBounds.top + radius * 2) ||
                position.y in (visibleBounds.bottom - radius * 2)..visibleBounds.bottom
            val atVerticalEdge = position.x in visibleBounds.left..(visibleBounds.left + radius * 2) ||
                position.x in (visibleBounds.right - radius * 2)..visibleBounds.right
            val dx = if (symmetry.horizontal && atHorizontalEdge) abs(position.x - x) else Float.POSITIVE_INFINITY
            val dy = if (symmetry.vertical && atVerticalEdge) abs(position.y - y) else Float.POSITIVE_INFINITY
            if (minOf(dx, dy) > radius) return null
            val isXAxis = dx <= dy
            return SymmetryAxisDrag(isXAxis, if (isXAxis) position.x else position.y,
                if (isXAxis) {
                    (symmetry.x(width) * cellWidth).coerceIn(visibleBounds.left, visibleBounds.right) / cellWidth
                } else {
                    (symmetry.y(height) * cellHeight).coerceIn(visibleBounds.top, visibleBounds.bottom) / cellHeight
                },
                if (isXAxis) cellWidth else cellHeight, if (isXAxis) width else height, onMove)
        }
    }
}
