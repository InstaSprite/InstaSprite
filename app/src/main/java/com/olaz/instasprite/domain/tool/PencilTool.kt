package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.utils.bresenhamLine

object PencilTool : StrokeTool {
    override val icon: Int = R.drawable.ic_pencil_tool
    override val name: String = "Pencil"
    override val description: String = "Draw on the canvas"
    override val commitsImmediately: Boolean = false

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
        lastRow = row
        lastCol = col
        strokeColor = color.toArgb()
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()
        accumulated.clear()

        val changes = brushPixels(row, col)
        accumulated.addAll(changes)
        return StrokeUpdate(changes)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        val points = bresenhamLine(lastCol, lastRow, col, row)
        val newChanges = mutableListOf<PixelChange>()
        for ((px, py) in points) {
            newChanges.addAll(brushPixels(py, px))
        }
        accumulated.addAll(newChanges)
        lastRow = row
        lastCol = col
        return StrokeUpdate(newChanges)
    }

    override fun endStroke(): List<PixelChange> {
        return accumulated.toList()
    }

    override fun cancelStroke() {
        accumulated.clear()
        lastRow = 0
        lastCol = 0
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