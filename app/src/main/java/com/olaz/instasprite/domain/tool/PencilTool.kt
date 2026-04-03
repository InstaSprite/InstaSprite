package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.utils.bresenhamLine

object PencilTool : StrokeTool {
    override val icon: Int = R.drawable.ic_pencil_tool
    override val name: String = "Pencil"
    override val description: String = "Draw on the canvas"
    override val commitsImmediately: Boolean = false

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
        lastRow = row
        lastCol = col
        strokeColor = color.toArgb()
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()
        accumulatedByPixel.clear()

        val newChanges = ArrayList<PixelChange>()
        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            addIfNew(r, c, newChanges)
        }
        return StrokeUpdate(newChanges)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        val points = bresenhamLine(lastCol, lastRow, col, row)
        val newChanges = ArrayList<PixelChange>()
        for ((px, py) in points) {
            forEachBrushPixel(py, px, strokeScale, canvasWidth, canvasHeight) { r, c ->
                addIfNew(r, c, newChanges)
            }
        }
        lastRow = row
        lastCol = col
        return StrokeUpdate(newChanges)
    }

    override fun endStroke(): List<PixelChange> {
        return accumulatedByPixel.values.toList()
    }

    override fun cancelStroke() {
        accumulatedByPixel.clear()
        lastRow = 0
        lastCol = 0
    }

    private fun addIfNew(row: Int, col: Int, out: MutableList<PixelChange>) {
        val key = row * canvasWidth + col
        if (accumulatedByPixel.containsKey(key)) return
        val change = PixelChange(row, col, strokeColor)
        accumulatedByPixel[key] = change
        out.add(change)
    }

}