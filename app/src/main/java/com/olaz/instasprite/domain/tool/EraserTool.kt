package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.utils.bresenhamLine

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
        canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int
    ): StrokeUpdate {
        lastRow = row
        lastCol = col
        strokeScale = scale
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()

        canvas.setPixel(row, col, Color.Transparent, scale)
        val changes = ArrayList<PixelChange>()
        val seen = HashSet<Int>()
        forEachBrushPixel(row, col, strokeScale, canvasWidth, canvasHeight) { r, c ->
            val key = r * canvasWidth + c
            if (seen.add(key)) {
                changes.add(PixelChange(r, c, Color.Transparent.toArgb()))
            }
        }
        return StrokeUpdate(changes)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase, row: Int, col: Int
    ): StrokeUpdate {
        val points = bresenhamLine(lastCol, lastRow, col, row)
        val allChanges = ArrayList<PixelChange>()
        val seen = HashSet<Int>()
        for ((px, py) in points) {
            canvas.setPixel(py, px, Color.Transparent, strokeScale)
            forEachBrushPixel(py, px, strokeScale, canvasWidth, canvasHeight) { r, c ->
                val key = r * canvasWidth + c
                if (seen.add(key)) {
                    allChanges.add(PixelChange(r, c, Color.Transparent.toArgb()))
                }
            }
        }
        lastRow = row
        lastCol = col
        return StrokeUpdate(allChanges)
    }

    override fun endStroke(): List<PixelChange> {
        return emptyList()
    }

    override fun cancelStroke() {
        lastRow = 0
        lastCol = 0
    }

}