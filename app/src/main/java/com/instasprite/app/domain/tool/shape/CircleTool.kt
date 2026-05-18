package com.instasprite.app.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.R
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.tool.forEachBrushPixel
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlin.math.pow
import kotlin.math.sqrt

object CircleTool : ShapeTool {
    override val icon: Int = R.drawable.ic_circle_tool
    override val name: String = "Circle"
    override val description: String = "Draw circles"
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

        val dx = (lastCol - startCol).toDouble()
        val dy = (lastRow - startRow).toDouble()
        val radius = sqrt(dx.pow(2) + dy.pow(2)).toInt()

        if (radius == 0) {
            stampBrush(startRow, startCol, plotPreviewPixel)
            return StrokeUpdate(isFullPreview = true)
        }

        var x = 0
        var y = radius
        var d = 3 - 2 * radius

        stampCirclePoints(startCol, startRow, x, y, plotPreviewPixel)

        while (y >= x) {
            x++
            if (d > 0) {
                y--
                d += 4 * (x - y) + 10
            } else {
                d += 4 * x + 6
            }
            stampCirclePoints(startCol, startRow, x, y, plotPreviewPixel)
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

    private fun stampCirclePoints(
        cx: Int,
        cy: Int,
        x: Int,
        y: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit
    ) {
        stampBrush(cy + y, cx + x, plotPreviewPixel)
        stampBrush(cy + y, cx - x, plotPreviewPixel)
        stampBrush(cy - y, cx + x, plotPreviewPixel)
        stampBrush(cy - y, cx - x, plotPreviewPixel)
        stampBrush(cy + x, cx + y, plotPreviewPixel)
        stampBrush(cy + x, cx - y, plotPreviewPixel)
        stampBrush(cy - x, cx + y, plotPreviewPixel)
        stampBrush(cy - x, cx - y, plotPreviewPixel)
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