package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.PixelPerfectFilter
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

    var isPixelPerfect: Boolean = false

    // Whether pixel-perfect is actually active for the current stroke (requires scale == 1)
    private var usePixelPerfect = false

    private val pixelPerfectFilter = PixelPerfectFilter()

    private var activePlotPreview: ((Int, Int, Int) -> Unit)? = null

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

        // Pixel-perfect only makes sense at brush size 1.
        usePixelPerfect = isPixelPerfect && scale == 1
        activePlotPreview = if (usePixelPerfect) plotPreviewPixel else null

        if (usePixelPerfect) {
            pixelPerfectFilter.start(row, col)
        } else {
            stampBrush(row, col, plotPreviewPixel)
        }

        return StrokeUpdate()
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        if (usePixelPerfect) {
            activePlotPreview = plotPreviewPixel
            bresenhamLine(lastCol, lastRow, col, row) { px, py ->
                pixelPerfectFilter.addPoint(py, px) { r, c ->
                    stampBrush(r, c, plotPreviewPixel)
                }
            }
        } else {
            bresenhamLine(lastCol, lastRow, col, row) { px, py ->
                stampBrush(py, px, plotPreviewPixel)
            }
        }
        lastRow = row
        lastCol = col
        return StrokeUpdate()
    }

    override fun endStroke() {
        if (usePixelPerfect) {
            val plot = activePlotPreview
            if (plot != null) {
                pixelPerfectFilter.end { r, c ->
                    stampBrush(r, c, plot)
                }
            } else {
                pixelPerfectFilter.clear()
            }
        }
        activePlotPreview = null
    }

    override fun cancelStroke() {
        lastRow = 0
        lastCol = 0
        if (usePixelPerfect) {
            pixelPerfectFilter.clear()
        }
        activePlotPreview = null
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