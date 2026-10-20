package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.draw.MoveCompositor
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

class MoveTool : StrokeTool {
    override val type = ToolType.Move
    override val commitsImmediately = false
    override val staysPendingAfterStroke = true
    private var session: MoveSession? = null
    val isMoving: Boolean get() = session != null

    fun reset() {
        session = null
    }

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {}

    override fun beginStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        plotPreviewPixel: (Int, Int, Int) -> Unit,
        onCommittedPixel: (Int, Int) -> Unit
    ): StrokeUpdate {
        val current = session ?: run {
            val layer = canvas.getLayers().firstOrNull { it.id == canvas.getActiveLayerId() } ?: return StrokeUpdate()
            if (layer.isLocked || !layer.isVisible) return StrokeUpdate()
            MoveSession(MoveCompositor(canvas.getCanvasWidth(), canvas.getCanvasHeight(),
                canvas.getLayers().toList(), layer.id, canvas.getSelectionMask())).also { session = it }
        }
        current.startCol = col
        current.startRow = row
        current.accumulatedOffsetX = current.offsetX
        current.accumulatedOffsetY = current.offsetY
        return current.preview()
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (Int, Int, Int) -> Unit,
        onCommittedPixel: (Int, Int) -> Unit
    ): StrokeUpdate {
        val current = session ?: return StrokeUpdate()
        val offsetX = current.accumulatedOffsetX + (col - current.startCol)
        val offsetY = current.accumulatedOffsetY + (row - current.startRow)
        if (offsetX == current.offsetX && offsetY == current.offsetY) return StrokeUpdate()
        current.offsetX = offsetX
        current.offsetY = offsetY
        return current.preview()
    }

    override fun endStroke() {}

    override fun cancelStroke() = reset()

    override fun commitPending(canvas: PixelCanvasUseCase): StrokeUpdate? {
        val current = session ?: return null
        session = null
        return StrokeUpdate(mainLayerTiles = current.compositor.tiles)
    }

    override fun cancelPending(canvas: PixelCanvasUseCase): StrokeUpdate? {
        val current = session ?: return null
        session = null
        return StrokeUpdate(
            previewRegions = current.compositor.composite(0, 0),
            updatedSelectionMask = current.compositor.selectionMask
        )
    }

    private class MoveSession(
        val compositor: MoveCompositor,
        var startCol: Int = 0,
        var startRow: Int = 0,
        var offsetX: Int = 0,
        var offsetY: Int = 0,
        var accumulatedOffsetX: Int = 0,
        var accumulatedOffsetY: Int = 0
    ) {
        fun preview() = StrokeUpdate(
            previewRegions = compositor.composite(offsetX, offsetY),
            updatedSelectionMask = compositor.shiftedSelectionMask(offsetX, offsetY)
        )
    }
}
