package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

data class StrokeUpdate(
    val isFullPreview: Boolean = false,
    val overlayPixels: IntArray? = null,
    val mainLayerPixels: IntArray? = null,
    val updatedSelectionMask: BooleanArray? = null
)

interface StrokeTool : Tool {
    val commitsImmediately: Boolean

    fun beginStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate

    fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate

    fun endStroke()
    fun cancelStroke()

    fun commitPending(canvas: PixelCanvasUseCase): StrokeUpdate? = null
    fun cancelPending(canvas: PixelCanvasUseCase): StrokeUpdate? = null
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

    // Skip if the brush area is completely out of bounds after clamping
    if (rStart > rEnd || cStart > cEnd) {
        return
    }

    for (r in rStart..rEnd) {
        for (c in cStart..cEnd) {
            action(r, c)
        }
    }
}
