package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

object MoveTool : StrokeTool {
    override val icon: Int = R.drawable.ic_move_tool
    override val name: String = "Move"
    override val description: String = "Move layer or selection"
    override val commitsImmediately: Boolean = false
    override val staysPendingAfterStroke: Boolean = true

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
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        val existing = session
        if (existing != null) {
            existing.startCol = col
            existing.startRow = row
            existing.accumulatedOffsetX = existing.offsetX
            existing.accumulatedOffsetY = existing.offsetY
            return StrokeUpdate(
                isFullPreview = true,
                overlayPixels = existing.computeShiftedPixels()
            )
        }
        val w = canvas.getCanvasWidth()
        val h = canvas.getCanvasHeight()
        val pixels = canvas.getActiveLayerPixelsDirect()!!.copyOf()
        val mask = canvas.getSelectionMask()
        val s = MoveSession(pixels, mask, w, h, startCol = col, startRow = row)
        session = s
        return StrokeUpdate(
            isFullPreview = true,
            overlayPixels = s.computeShiftedPixels(),
            mainLayerPixels = s.computeClearedPixels()
        )
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        val s = session ?: return StrokeUpdate()
        s.offsetX = s.accumulatedOffsetX + (col - s.startCol)
        s.offsetY = s.accumulatedOffsetY + (row - s.startRow)
        return StrokeUpdate(
            isFullPreview = true,
            overlayPixels = s.computeShiftedPixels(),
            updatedSelectionMask = s.computeShiftedMask()
        )
    }

    override fun endStroke() {}

    override fun cancelStroke() {
        session = null
    }

    override fun commitPending(canvas: PixelCanvasUseCase): StrokeUpdate? {
        val s = session ?: return null
        val result = StrokeUpdate(
            mainLayerPixels = s.computeShiftedPixels(),
            updatedSelectionMask = s.computeShiftedMask(),
            overlayPixels = IntArray(0)
        )
        session = null
        return result
    }

    override fun cancelPending(canvas: PixelCanvasUseCase): StrokeUpdate? {
        val s = session ?: return null
        val result = StrokeUpdate(
            mainLayerPixels = s.originalPixels,
            updatedSelectionMask = s.selectionMask,
            overlayPixels = IntArray(0)
        )
        session = null
        return result
    }

    private class MoveSession(
        val originalPixels: IntArray,
        val selectionMask: BooleanArray?,
        val canvasWidth: Int,
        val canvasHeight: Int,
        var startCol: Int,
        var startRow: Int,
        var offsetX: Int = 0,
        var offsetY: Int = 0,
        var accumulatedOffsetX: Int = 0,
        var accumulatedOffsetY: Int = 0
    ) {
        fun computeShiftedPixels(): IntArray {
            val w = canvasWidth
            val h = canvasHeight
            val result = if (selectionMask != null) {
                originalPixels.copyOf().also { r ->
                    for (i in selectionMask.indices) {
                        if (selectionMask[i]) r[i] = 0
                    }
                }
            } else {
                IntArray(w * h)
            }
            for (srcY in 0 until h) {
                for (srcX in 0 until w) {
                    val srcIdx = srcY * w + srcX
                    if (selectionMask != null && !selectionMask[srcIdx]) continue
                    val pixel = originalPixels[srcIdx]
                    if (pixel == 0) continue
                    val dstX = srcX + offsetX
                    val dstY = srcY + offsetY
                    if (dstX in 0 until w && dstY in 0 until h) {
                        result[dstY * w + dstX] = pixel
                    }
                }
            }
            return result
        }

        fun computeClearedPixels(): IntArray {
            val w = canvasWidth
            val h = canvasHeight
            val result = originalPixels.copyOf()
            if (selectionMask != null) {
                for (i in selectionMask.indices) {
                    if (selectionMask[i]) result[i] = 0
                }
            } else {
                result.fill(0)
            }
            return result
        }

        fun computeShiftedMask(): BooleanArray? {
            val mask = selectionMask ?: return null
            val w = canvasWidth
            val h = canvasHeight
            val result = BooleanArray(w * h)
            for (srcY in 0 until h) {
                for (srcX in 0 until w) {
                    val srcIdx = srcY * w + srcX
                    if (!mask[srcIdx]) continue
                    val dstX = srcX + offsetX
                    val dstY = srcY + offsetY
                    if (dstX in 0 until w && dstY in 0 until h) {
                        result[dstY * w + dstX] = true
                    }
                }
            }
            return result
        }
    }
}