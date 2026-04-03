package com.olaz.instasprite.domain.tool.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeUpdate
import com.olaz.instasprite.domain.tool.forEachBrushPixel
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
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

        rebuildCirclePreview(startRow, startCol, lastRow, lastCol)
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

    private fun rebuildCirclePreview(r1: Int, c1: Int, r2: Int, c2: Int) {
        val next = LinkedHashMap<Int, PixelChange>()

        val dx = (c2 - c1).toDouble()
        val dy = (r2 - r1).toDouble()
        val radius = sqrt(dx.pow(2) + dy.pow(2)).toInt()

        if (radius == 0) {
            stampBrush(next, r1, c1)
            accumulatedByPixel.clear()
            accumulatedByPixel.putAll(next)
            return
        }

        var x = 0
        var y = radius
        var d = 3 - 2 * radius

        stampCirclePoints(next, c1, r1, x, y)

        while (y >= x) {
            x++
            if (d > 0) {
                y--
                d += 4 * (x - y) + 10
            } else {
                d += 4 * x + 6
            }
            stampCirclePoints(next, c1, r1, x, y)
        }

        accumulatedByPixel.clear()
        accumulatedByPixel.putAll(next)
    }

    private fun stampCirclePoints(target: MutableMap<Int, PixelChange>, cx: Int, cy: Int, x: Int, y: Int) {
        stampBrush(target, cy + y, cx + x)
        stampBrush(target, cy + y, cx - x)
        stampBrush(target, cy - y, cx + x)
        stampBrush(target, cy - y, cx - x)
        stampBrush(target, cy + x, cx + y)
        stampBrush(target, cy + x, cx - y)
        stampBrush(target, cy - x, cx + y)
        stampBrush(target, cy - x, cx - y)
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