package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

object OvalTool : ShapeTool {
    override val icon: Int = R.drawable.ic_oval_tool
    override val name: String = "Oval"
    override val description: String = "Draw oval shapes"
    override val commitsImmediately: Boolean = false

    private var startRow = 0
    private var startCol = 0
    private var lastRow = 0
    private var lastCol = 0
    private var strokeColor: Int = 0
    private var strokeScale: Int = 1
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private val accumulated = mutableListOf<PixelChange>()

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        canvas.setPixel(row, col, color)
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, size: Int) {
        canvas.setPixel(row, col, color, size)
    }

    override fun beginStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int
    ): StrokeUpdate {
        startRow = row
        startCol = col
        lastRow = row
        lastCol = col
        strokeColor = color.toArgb()
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()
        accumulated.clear()

        val changes = brushPixels(row, col)
        accumulated.addAll(changes)
        return StrokeUpdate(changes, isFullPreview = true)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        lastRow = row
        lastCol = col
        
        val newChanges = generateOval(startRow, startCol, lastRow, lastCol)
        
        accumulated.clear()
        accumulated.addAll(newChanges)
        
        return StrokeUpdate(newChanges, isFullPreview = true)
    }

    override fun endStroke(): List<PixelChange> {
        val result = accumulated.toList()
        accumulated.clear()
        return result
    }

    override fun cancelStroke() {
        accumulated.clear()
        startRow = 0
        startCol = 0
        lastRow = 0
        lastCol = 0
    }

    private fun generateOval(r1: Int, c1: Int, r2: Int, c2: Int): List<PixelChange> {
        val changes = mutableListOf<PixelChange>()

        val minCol = minOf(c1, c2)
        val maxCol = maxOf(c1, c2)
        val minRow = minOf(r1, r2)
        val maxRow = maxOf(r1, r2)

        val w = maxCol - minCol
        val h = maxRow - minRow

        // Center coordinates
        val cx = minCol + w / 2
        val cy = minRow + h / 2

        // Semi-axes
        val a = w / 2
        val b = h / 2

        if (a == 0 && b == 0) {
            changes.addAll(brushPixels(r1, c1))
            return changes
        }

        // Midpoint ellipse algorithm
        var x = 0
        var y = b
        var d1 = b * b - a * a * b + a * a / 4

        drawOvalPoints(cx, cy, x, y, changes)

        // Region 1
        while (a * a * (y - 0.5) > b * b * (x + 1)) {
            if (d1 < 0) {
                d1 += b * b * (2 * x + 3)
            } else {
                d1 += b * b * (2 * x + 3) + a * a * (-2 * y + 2)
                y--
            }
            x++
            drawOvalPoints(cx, cy, x, y, changes)
        }

        // Region 2
        var d2 = b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b
        while (y > 0) {
            if (d2 < 0) {
                d2 += b * b * (2 * x + 2) + a * a * (-2 * y + 3)
                x++
            } else {
                d2 += a * a * (-2 * y + 3)
            }
            y--
            drawOvalPoints(cx, cy, x, y, changes)
        }

        return changes.distinct()
    }

    private fun drawOvalPoints(cx: Int, cy: Int, x: Int, y: Int, changes: MutableList<PixelChange>) {
        changes.addAll(brushPixels(cy + y, cx + x))
        changes.addAll(brushPixels(cy + y, cx - x))
        changes.addAll(brushPixels(cy - y, cx + x))
        changes.addAll(brushPixels(cy - y, cx - x))
    }

    private fun brushPixels(row: Int, col: Int): List<PixelChange> {
        val result = mutableListOf<PixelChange>()
        var rStart = row; var rEnd = row
        var cStart = col; var cEnd = col
        for (s in 2..strokeScale) {
            if (s % 2 == 0) { rStart--; cStart-- } else { rEnd++; cEnd++ }
        }
        rStart = rStart.coerceAtLeast(0)
        cStart = cStart.coerceAtLeast(0)
        rEnd = rEnd.coerceAtMost(canvasHeight - 1)
        cEnd = cEnd.coerceAtMost(canvasWidth - 1)
        for (r in rStart..rEnd) {
            for (c in cStart..cEnd) {
                result.add(PixelChange(r, c, strokeColor))
            }
        }
        return result
    }
}
