package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

data class PixelChange(val row: Int, val col: Int, val color: Int)

data class StrokeUpdate(
    val changes: List<PixelChange>,
    val isFullPreview: Boolean = false
)

interface StrokeTool : Tool {
    val commitsImmediately: Boolean

    fun beginStroke(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int): StrokeUpdate
    fun updateStroke(canvas: PixelCanvasUseCase, row: Int, col: Int): StrokeUpdate
    fun endStroke(): List<PixelChange>
    fun cancelStroke()
}

interface ShapeTool : StrokeTool {

}

inline fun forEachBrushPixel(
    row: Int,
    col: Int,
    scale: Int,
    canvasWidth: Int,
    canvasHeight: Int,
    action: (Int, Int) -> Unit
) {
    var rStart = row
    var rEnd = row
    var cStart = col
    var cEnd = col

    for (s in 2..scale) {
        if (s % 2 == 0) {
            rStart -= 1
            cStart -= 1
        } else {
            rEnd += 1
            cEnd += 1
        }
    }

    rStart = rStart.coerceAtLeast(0)
    cStart = cStart.coerceAtLeast(0)
    rEnd = rEnd.coerceAtMost(canvasHeight - 1)
    cEnd = cEnd.coerceAtMost(canvasWidth - 1)

    for (r in rStart..rEnd) {
        for (c in cStart..cEnd) {
            action(r, c)
        }
    }
}
