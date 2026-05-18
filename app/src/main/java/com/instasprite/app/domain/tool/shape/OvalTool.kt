package com.instasprite.app.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.R
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.tool.forEachBrushPixel
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlin.math.roundToInt

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

        val minCol = minOf(startCol, lastCol)
        val maxCol = maxOf(startCol, lastCol)
        val minRow = minOf(startRow, lastRow)
        val maxRow = maxOf(startRow, lastRow)

        val rx = (maxCol - minCol) / 2.0
        val ry = (maxRow - minRow) / 2.0
        val cx = (minCol + maxCol) / 2.0
        val cy = (minRow + maxRow) / 2.0

        if (rx == 0.0 && ry == 0.0) {
            stampBrush(startRow, startCol, plotPreviewPixel)
            return StrokeUpdate(isFullPreview = true)
        }

        val rxSq = rx * rx
        val rySq = ry * ry

        var x = 0.0
        var y = ry
        var dx = 2.0 * rySq * x
        var dy = 2.0 * rxSq * y

        var d1 = rySq - rxSq * ry + 0.25 * rxSq

        stampOvalPoints(cx, cy, x, y, plotPreviewPixel)

        while (dx < dy) {
            if (d1 < 0) {
                x += 1.0
                dx += 2.0 * rySq
                d1 += dx + rySq
            } else {
                x += 1.0
                y -= 1.0
                dx += 2.0 * rySq
                dy -= 2.0 * rxSq
                d1 += dx - dy + rySq
            }
            stampOvalPoints(cx, cy, x, y, plotPreviewPixel)
        }

        var d2 =
            rySq * (x + 0.5) * (x + 0.5) + rxSq * (y - 1.0) * (y - 1.0) - rxSq * rySq
        while (y >= 0.0) {
            if (d2 < 0) {
                x += 1.0
                y -= 1.0
                dx += 2.0 * rySq
                dy -= 2.0 * rxSq
                d2 += dx - dy + rxSq
            } else {
                y -= 1.0
                dy -= 2.0 * rxSq
                d2 += rxSq - dy
            }
            stampOvalPoints(cx, cy, x, y, plotPreviewPixel)
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

    private fun stampOvalPoints(
        cx: Double,
        cy: Double,
        x: Double,
        y: Double,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit
    ) {
        stampBrush((cy + y).roundToInt(), (cx + x).roundToInt(), plotPreviewPixel)
        stampBrush((cy + y).roundToInt(), (cx - x).roundToInt(), plotPreviewPixel)
        stampBrush((cy - y).roundToInt(), (cx + x).roundToInt(), plotPreviewPixel)
        stampBrush((cy - y).roundToInt(), (cx - x).roundToInt(), plotPreviewPixel)
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
