package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.tool.forEachBrushPixel
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.utils.bresenhamLine

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

        stampBrush(accumulatedByPixel, row, col)
        return StrokeUpdate(accumulatedByPixel.values.toList(), isFullPreview = true)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        lastRow = row
        lastCol = col

        rebuildDiamondPreview(startRow, startCol, lastRow, lastCol)
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

    private fun rebuildDiamondPreview(r1: Int, c1: Int, r2: Int, c2: Int) {
        val next = LinkedHashMap<Int, PixelChange>()

        val minRow = minOf(r1, r2)
        val maxRow = maxOf(r1, r2)
        val minCol = minOf(c1, c2)
        val maxCol = maxOf(c1, c2)

        if (minRow == maxRow && minCol == maxCol) {
            stampBrush(next, r1, c1)
            accumulatedByPixel.clear()
            accumulatedByPixel.putAll(next)
            return
        }

        val midRow = minRow + (maxRow - minRow) / 2
        val midCol = minCol + (maxCol - minCol) / 2

        val points = mutableListOf<Pair<Int, Int>>()
        points.addAll(bresenhamLine(midCol, minRow, maxCol, midRow))
        points.addAll(bresenhamLine(maxCol, midRow, midCol, maxRow))
        points.addAll(bresenhamLine(midCol, maxRow, minCol, midRow))
        points.addAll(bresenhamLine(minCol, midRow, midCol, minRow))

        for ((px, py) in points) {
            stampBrush(next, py, px)
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
