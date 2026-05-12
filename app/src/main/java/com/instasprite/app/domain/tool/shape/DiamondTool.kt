package com.instasprite.app.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.R
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.tool.forEachBrushPixel
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.bresenhamLine

object DiamondTool : ShapeTool {
    override val icon: Int = R.drawable.ic_diamond_tool
    override val name: String = "Diamond"
    override val description: String = "Draw diamond shapes"
    override val commitsImmediately: Boolean = false

    private var startRow = 0
    private var startCol = 0
    private var lastRow = 0
    private var lastCol = 0
    private var strokeColor: Int = 0
    private var strokeScale: Int = 1
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        canvas.setPixel(row, col, color)
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {
        canvas.setPixel(row, col, color, scale)
    }

    override fun beginStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        startRow = row
        startCol = col
        lastRow = row
        lastCol = col
        strokeColor = color.toArgb()
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()

        stampBrush(row, col, plotPreviewPixel)
        return StrokeUpdate(isFullPreview = true)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        lastRow = row
        lastCol = col

        val minRow = minOf(startRow, lastRow)
        val maxRow = maxOf(startRow, lastRow)
        val minCol = minOf(startCol, lastCol)
        val maxCol = maxOf(startCol, lastCol)

        if (minRow == maxRow && minCol == maxCol) {
            stampBrush(startRow, startCol, plotPreviewPixel)
            return StrokeUpdate(isFullPreview = true)
        }

        val midRow = minRow + (maxRow - minRow) / 2
        val midCol = minCol + (maxCol - minCol) / 2

        val points = mutableListOf<Pair<Int, Int>>()
        points.addAll(bresenhamLine(midCol, minRow, maxCol, midRow))
        points.addAll(bresenhamLine(maxCol, midRow, midCol, maxRow))
        points.addAll(bresenhamLine(midCol, maxRow, minCol, midRow))
        points.addAll(bresenhamLine(minCol, midRow, midCol, minRow))

        for ((px, py) in points) {
            stampBrush(py, px, plotPreviewPixel)
        }

        return StrokeUpdate(isFullPreview = true)
    }

    override fun endStroke() {
    }

    override fun cancelStroke() {
        startRow = 0
        startCol = 0
        lastRow = 0
        lastCol = 0
    }

    private fun stampBrush(
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit
    ) {
        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            plotPreviewPixel(r, c, strokeColor)
        }
    }
}
