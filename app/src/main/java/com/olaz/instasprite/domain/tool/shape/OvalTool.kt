package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.tool.forEachBrushPixel
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

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

        rebuildOvalPreview(startRow, startCol, lastRow, lastCol)
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

    private fun rebuildOvalPreview(r1: Int, c1: Int, r2: Int, c2: Int) {
        val next = LinkedHashMap<Int, PixelChange>()

        val minCol = minOf(c1, c2)
        val maxCol = maxOf(c1, c2)
        val minRow = minOf(r1, r2)
        val maxRow = maxOf(r1, r2)

        val w = maxCol - minCol
        val h = maxRow - minRow

        val cx = minCol + w / 2
        val cy = minRow + h / 2

        val a = w / 2
        val b = h / 2

        if (a == 0 && b == 0) {
            stampBrush(next, r1, c1)
            accumulatedByPixel.clear()
            accumulatedByPixel.putAll(next)
            return
        }

        var x = 0
        var y = b
        var d1 = b * b - a * a * b + a * a / 4

        stampOvalPoints(next, cx, cy, x, y)

        while (a * a * (y - 0.5) > b * b * (x + 1)) {
            if (d1 < 0) {
                d1 += b * b * (2 * x + 3)
            } else {
                d1 += b * b * (2 * x + 3) + a * a * (-2 * y + 2)
                y--
            }
            x++
            stampOvalPoints(next, cx, cy, x, y)
        }

        var d2 = b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b
        while (y > 0) {
            if (d2 < 0) {
                d2 += b * b * (2 * x + 2) + a * a * (-2 * y + 3)
                x++
            } else {
                d2 += a * a * (-2 * y + 3)
            }
            y--
            stampOvalPoints(next, cx, cy, x, y)
        }

        accumulatedByPixel.clear()
        accumulatedByPixel.putAll(next)
    }

    private fun stampOvalPoints(target: MutableMap<Int, PixelChange>, cx: Int, cy: Int, x: Int, y: Int) {
        stampBrush(target, cy + y, cx + x)
        stampBrush(target, cy + y, cx - x)
        stampBrush(target, cy - y, cx + x)
        stampBrush(target, cy - y, cx - x)
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
