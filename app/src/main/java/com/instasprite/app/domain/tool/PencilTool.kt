package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.bresenhamLine

object PencilTool : StrokeTool {
    override val icon: Int = R.drawable.ic_pencil_tool
    override val nameRes: Int = R.string.pencil
    override val descriptionRes: Int = R.string.tool_pencil_desc
    override val commitsImmediately: Boolean = false

    private var lastRow = 0
    private var lastCol = 0
    private var strokeColor: Int = 0
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var stamp: BrushStamp = BrushStamp.create(BrushShape.Square, 1)

    var brushShape: BrushShape = BrushShape.Square

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
        lastRow = row
        lastCol = col
        strokeColor = color.toArgb()
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()
        stamp = BrushStamp.create(brushShape, scale)

        stampBrush(row, col, plotPreviewPixel)
        return StrokeUpdate()
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        bresenhamLine(lastCol, lastRow, col, row) { px, py ->
            stampBrush(py, px, plotPreviewPixel)
        }
        lastRow = row
        lastCol = col
        return StrokeUpdate()
    }

    override fun endStroke() {}

    override fun cancelStroke() {
        lastRow = 0
        lastCol = 0
    }

    private fun stampBrush(
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit
    ) {
        stamp.forEach(row, col, canvasWidth, canvasHeight) { r, c ->
            plotPreviewPixel(r, c, strokeColor)
        }
    }
}