package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.tool.forEachBrushPixel
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

    private val accumulatedByPixel = LinkedHashMap<Int, PixelChange>()

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        canvas.setPixel(row, col, color)
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {
        canvas.setPixel(row, col, color, scale)
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
        accumulatedByPixel.clear()

        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            addIfNew(accumulatedByPixel, r, c)
        }
        return StrokeUpdate(accumulatedByPixel.values.toList(), isFullPreview = true)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        lastRow = row
        lastCol = col

        rebuildRectanglePreview(startRow, startCol, lastRow, lastCol)
        return StrokeUpdate(accumulatedByPixel.values.toList(), isFullPreview = true)
    }

    override fun endStroke(): List<PixelChange> {
        val result = accumulatedByPixel.values.toList()
        accumulatedByPixel.clear()
        return result
    }

    override fun cancelStroke() {
        accumulatedByPixel.clear()
        startRow = 0
        startCol = 0
        lastRow = 0
        lastCol = 0
    }

    private fun rebuildRectanglePreview(r1: Int, c1: Int, r2: Int, c2: Int) {
        val next = LinkedHashMap<Int, PixelChange>()

        val minRow = minOf(r1, r2)
        val maxRow = maxOf(r1, r2)
        val minCol = minOf(c1, c2)
        val maxCol = maxOf(c1, c2)

        for (c in minCol..maxCol) {
            stampBrush(next, minRow, c)
            stampBrush(next, maxRow, c)
        }

        for (r in (minRow + 1) until maxRow) {
            stampBrush(next, r, minCol)
            stampBrush(next, r, maxCol)
        }

        accumulatedByPixel.clear()
        accumulatedByPixel.putAll(next)
    }

    private fun stampBrush(target: MutableMap<Int, PixelChange>, row: Int, col: Int) {
        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            addIfNew(target, r, c)
        }
    }

    private fun addIfNew(target: MutableMap<Int, PixelChange>, row: Int, col: Int) {
        val key = row * canvasWidth + col
        if (!target.containsKey(key)) {
            target[key] = PixelChange(row, col, strokeColor)
        }
    }
}