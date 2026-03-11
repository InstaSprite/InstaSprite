package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

object RectangleTool : ShapeTool {
    override val icon: Int = R.drawable.ic_rect_tool
    override val name: String = "Rectangle"
    override val description: String = "Draw rectangle shapes"
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

        val newChanges = generateRectangle(startRow, startCol, lastRow, lastCol)

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

    private fun generateRectangle(r1: Int, c1: Int, r2: Int, c2: Int): List<PixelChange> {
        val changes = mutableListOf<PixelChange>()

        // Define corners
        val minRow = minOf(r1, r2)
        val maxRow = maxOf(r1, r2)
        val minCol = minOf(c1, c2)
        val maxCol = maxOf(c1, c2)

        // Draw top and bottom edges
        for (c in minCol..maxCol) {
            changes.addAll(brushPixels(minRow, c))
            changes.addAll(brushPixels(maxRow, c))
        }

        // Draw left and right edges (excluding corners already drawn)
        for (r in (minRow + 1) until maxRow) {
            changes.addAll(brushPixels(r, minCol))
            changes.addAll(brushPixels(r, maxCol))
        }

        return changes.distinct()
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