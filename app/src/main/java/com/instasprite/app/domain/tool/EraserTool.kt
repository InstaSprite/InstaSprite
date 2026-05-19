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
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var stamp: BrushStamp = BrushStamp.create(BrushShape.Square, 1)

    private var touchedMask: BooleanArray = BooleanArray(0)
    private var batchIndices: IntArray = IntArray(0)
    private var batchColors: IntArray = IntArray(0)
    private var batchCount: Int = 0

    var brushShape: BrushShape = BrushShape.Square

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        canvas.setPixel(row, col, Color.Transparent, blend = false)
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {
        canvas.setPixel(row, col, Color.Transparent, scale, blend = false)
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
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()
        stamp = BrushStamp.create(brushShape, scale)

        val totalPixels = canvasWidth * canvasHeight
        if (touchedMask.size != totalPixels) {
            touchedMask = BooleanArray(totalPixels)
        }
        val bufferSize = minOf(totalPixels, stamp.count * (canvasWidth + canvasHeight))
        if (batchIndices.size < bufferSize) {
            batchIndices = IntArray(bufferSize)
            batchColors = IntArray(bufferSize)
        }

        batchCount = 0
        touchedMask.fill(false)
        collectBrushPixels(row, col)

        if (batchCount > 0) {
            canvas.batchSetPixels(batchIndices, batchColors, batchCount, blend = false)
            notifyCommittedPixels(onCommittedPixel)
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
        batchCount = 0
        touchedMask.fill(false)

        bresenhamLine(lastCol, lastRow, col, row) { px, py ->
            collectBrushPixels(py, px)
        }

        if (batchCount > 0) {
            canvas.batchSetPixels(batchIndices, batchColors, batchCount, blend = false)
            notifyCommittedPixels(onCommittedPixel)
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

    private inline fun collectBrushPixels(row: Int, col: Int) {
        stamp.forEach(row, col, canvasWidth, canvasHeight) { r, c ->
            val idx = r * canvasWidth + c
            if (!touchedMask[idx]) {
                touchedMask[idx] = true
                batchIndices[batchCount] = idx
                batchCount++
            }
        }
    }

    private inline fun notifyCommittedPixels(onCommittedPixel: (row: Int, col: Int) -> Unit) {
        for (i in 0 until batchCount) {
            val idx = batchIndices[i]
            onCommittedPixel(idx / canvasWidth, idx % canvasWidth)
        }
    }
}