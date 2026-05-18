package com.instasprite.app.domain.tool.selection

import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

interface SelectionTool {
    var currentSelection: SelectionState?
    fun clearSelection() {
        currentSelection = null
    }
}

object RectangleSelectionTool : StrokeTool, SelectionTool {
    override val icon: Int = R.drawable.ic_selection_tool
    override val name: String = "Rect"
    override val description: String = "Select a rectangular area"
    override val commitsImmediately: Boolean = false

    private var startRow = 0
    private var startCol = 0
    private var lastRow = 0
    private var lastCol = 0
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    enum class ResizeMode {
        NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, BOTTOM, LEFT, RIGHT
    }
    private var resizeMode = ResizeMode.NONE
    private var originalBounds: Rect? = null

    override var currentSelection: SelectionState? = null

    private val previewColor = 0xFFFFFFFF.toInt()

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {}
    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {}

    private var zoomScale: Float = 1f

    fun setZoomScale(scale: Float) {
        this.zoomScale = scale
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
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()

        resizeMode = ResizeMode.NONE
        if (currentSelection != null) {
            val b = currentSelection!!.bounds
            val left = b.left
            val right = b.right - 1
            val top = b.top
            val bottom = b.bottom - 1
            val midX = (left + right) / 2
            val midY = (top + bottom) / 2
            
            // Handle size is visually 16dp.
            // On a zoomed canvas, 1 canvas pixel = 1 * zoomScale screen pixels.
            // We use half of that for tolerance from the center point.
            var tol = maxOf(1, (6f / zoomScale).toInt())
            val maxTolX = maxOf(1, (right - left) / 3)
            val maxTolY = maxOf(1, (bottom - top) / 3)
            val tolX = minOf(tol, maxTolX)
            val tolY = minOf(tol, maxTolY)
            
            val nearLeft = Math.abs(col - left) <= tolX
            val nearRight = Math.abs(col - right) <= tolX
            val nearTop = Math.abs(row - top) <= tolY
            val nearBottom = Math.abs(row - bottom) <= tolY
            val nearMidX = Math.abs(col - midX) <= tolX
            val nearMidY = Math.abs(row - midY) <= tolY

            if (nearTop && nearLeft) resizeMode = ResizeMode.TOP_LEFT
            else if (nearTop && nearRight) resizeMode = ResizeMode.TOP_RIGHT
            else if (nearBottom && nearLeft) resizeMode = ResizeMode.BOTTOM_LEFT
            else if (nearBottom && nearRight) resizeMode = ResizeMode.BOTTOM_RIGHT
            else if (nearTop && nearMidX) resizeMode = ResizeMode.TOP
            else if (nearBottom && nearMidX) resizeMode = ResizeMode.BOTTOM
            else if (nearLeft && nearMidY) resizeMode = ResizeMode.LEFT
            else if (nearRight && nearMidY) resizeMode = ResizeMode.RIGHT
        }

        if (resizeMode != ResizeMode.NONE) {
            originalBounds = Rect(currentSelection!!.bounds)
            currentSelection = null
            lastRow = row
            lastCol = col
            return StrokeUpdate(isFullPreview = true)
        }

        startRow = row
        startCol = col
        lastRow = row
        lastCol = col
        currentSelection = null

        plotPreviewPixel(row, col, previewColor)
        return StrokeUpdate(isFullPreview = true)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        if (resizeMode != ResizeMode.NONE) {
            val b = originalBounds!!
            var minR = b.top
            var maxR = b.bottom - 1
            var minC = b.left
            var maxC = b.right - 1
            
            when (resizeMode) {
                ResizeMode.TOP_LEFT -> { minR = row; minC = col }
                ResizeMode.TOP_RIGHT -> { minR = row; maxC = col }
                ResizeMode.BOTTOM_LEFT -> { maxR = row; minC = col }
                ResizeMode.BOTTOM_RIGHT -> { maxR = row; maxC = col }
                ResizeMode.TOP -> { minR = row }
                ResizeMode.BOTTOM -> { maxR = row }
                ResizeMode.LEFT -> { minC = col }
                ResizeMode.RIGHT -> { maxC = col }
                else -> {}
            }
            
            startRow = minR
            lastRow = maxR
            startCol = minC
            lastCol = maxC
        } else {
            lastRow = row
            lastCol = col
        }

        val minRow = minOf(startRow, lastRow).coerceIn(0, canvasHeight - 1)
        val maxRow = maxOf(startRow, lastRow).coerceIn(0, canvasHeight - 1)
        val minCol = minOf(startCol, lastCol).coerceIn(0, canvasWidth - 1)
        val maxCol = maxOf(startCol, lastCol).coerceIn(0, canvasWidth - 1)

        for (c in minCol..maxCol) {
            plotPreviewPixel(minRow, c, previewColor)
            plotPreviewPixel(maxRow, c, previewColor)
        }
        for (r in (minRow + 1) until maxRow) {
            plotPreviewPixel(r, minCol, previewColor)
            plotPreviewPixel(r, maxCol, previewColor)
        }

        return StrokeUpdate(isFullPreview = true)
    }

    override fun endStroke() {
        val minRow = minOf(startRow, lastRow).coerceIn(0, canvasHeight - 1)
        val maxRow = maxOf(startRow, lastRow).coerceIn(0, canvasHeight - 1)
        val minCol = minOf(startCol, lastCol).coerceIn(0, canvasWidth - 1)
        val maxCol = maxOf(startCol, lastCol).coerceIn(0, canvasWidth - 1)

        val w = maxCol - minCol + 1
        val h = maxRow - minRow + 1
        
        if (w <= 0 || h <= 0) {
            currentSelection = null
            return
        }

        val mask = BooleanArray(canvasWidth * canvasHeight)
        for (r in minRow..maxRow) {
            val offset = r * canvasWidth
            for (c in minCol..maxCol) {
                mask[offset + c] = true
            }
        }

        currentSelection = SelectionState(
            mask = mask,
            bounds = Rect(minCol, minRow, maxCol + 1, maxRow + 1),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }

    override fun cancelStroke() {
        startRow = 0
        startCol = 0
        lastRow = 0
        lastCol = 0
        currentSelection = null
    }
}
