package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.bresenhamLine

object EraserTool : StrokeTool {
    override val icon: Int = R.drawable.ic_eraser_tool
    override val name: String = "Eraser"
    override val description: String = "Erase pixels on the canvas"
    override val commitsImmediately: Boolean = true

    private var lastRow = 0
    private var lastCol = 0
    private var strokeScale: Int = 1
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        canvas.setPixel(row, col, Color.Transparent)
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {
        canvas.setPixel(row, col, Color.Transparent, scale)
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
        lastRow = row
        lastCol = col
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()

        stampCommittedBrush(canvas, row, col, onCommittedPixel)
        return StrokeUpdate()
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        val points = bresenhamLine(lastCol, lastRow, col, row)
        for ((px, py) in points) {
            stampCommittedBrush(canvas, py, px, onCommittedPixel)
        }
        lastRow = row
        lastCol = col
        return StrokeUpdate()
    }

    override fun endStroke() {
        // No-op for immediate commit tools.
    }

    override fun cancelStroke() {
        lastRow = 0
        lastCol = 0
    }

    private fun stampCommittedBrush(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ) {
        val maxPixels = strokeScale * strokeScale
        val indices = IntArray(maxPixels)
        val colors = IntArray(maxPixels)
        var count = 0

        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            indices[count] = r * canvasWidth + c
            count++
            onCommittedPixel(r, c)
        }

        if (count > 0) {
            canvas.batchSetPixels(indices, colors, count)
        }
    }
}