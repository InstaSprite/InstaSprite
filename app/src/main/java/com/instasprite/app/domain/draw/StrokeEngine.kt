package com.instasprite.app.domain.draw

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.EraserTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class StrokeEndResult(
    val updatedSelectionState: SelectionState? = null,
    val shouldUpdateHistory: Boolean = false
)

class StrokeEngine(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    private val bitmapManager: BitmapManager,
    private val scope: CoroutineScope,
    private val onSelectionStateChanged: (SelectionState?) -> Unit
) {
    // Reused stroke buffers to avoid per-move allocations.
    private var strokeTouchMarks: IntArray = IntArray(0)
    private var strokeTouchedIndices: IntArray = IntArray(0)
    private var strokeTouchedCount: Int = 0
    private var strokeGeneration: Int = 1

    private fun ensureStrokeTrackingCapacity(width: Int, height: Int) {
        val total = width * height
        if (strokeTouchMarks.size != total) {
            strokeTouchMarks = IntArray(total)
            strokeGeneration = 1
        }
        if (strokeTouchedIndices.size < 1024) {
            strokeTouchedIndices = IntArray(1024)
        }
    }

    private fun restoreTouchedOverlayInMainBitmap() {
        if (strokeTouchedCount <= 0) return
        val bmp = bitmapManager.bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        for (i in 0 until strokeTouchedCount) {
            val idx = strokeTouchedIndices[i]
            val row = idx / w
            val col = idx % w
            if (row in 0 until h && col in 0 until w) {
                bmp[col, row] = pixelCanvasUseCase.getCompositedPixelAt(row, col)
            }
        }
    }

    private fun beginOverlayStrokeTracking() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        restoreTouchedOverlayInMainBitmap()
        ensureStrokeTrackingCapacity(w, h)
        strokeTouchedCount = 0
        strokeGeneration += 1
        if (strokeGeneration == Int.MAX_VALUE) {
            strokeTouchMarks.fill(0)
            strokeGeneration = 1
        }
        bitmapManager.clearOverlayBitmap()
    }

    private fun plotOverlayPixel(row: Int, col: Int, color: Int) {
        val bmp = bitmapManager.overlayBitmap ?: return
        if (row !in 0 until bmp.height || col !in 0 until bmp.width) return
        val index = row * bmp.width + col
        if (strokeTouchMarks[index] == strokeGeneration) return
        strokeTouchMarks[index] = strokeGeneration
        if (strokeTouchedCount >= strokeTouchedIndices.size) {
            strokeTouchedIndices = strokeTouchedIndices.copyOf(strokeTouchedIndices.size * 2)
        }
        strokeTouchedIndices[strokeTouchedCount++] = index
        bmp[col, row] = color

        val mainBmp = bitmapManager.bitmap ?: return
        mainBmp[col, row] = pixelCanvasUseCase.getPreviewCompositedPixelAt(row, col, color)
    }

    private fun applyCommittedPixelToMainBitmap(row: Int, col: Int) {
        val bmp = bitmapManager.bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (row !in 0 until h || col !in 0 until w) return
        val composited = pixelCanvasUseCase.getCompositedPixelAt(row, col)
        bmp[col, row] = composited
    }

    private fun commitOverlayStrokeToLayer() {
        if (strokeTouchedCount <= 0) return
        val bmp = bitmapManager.overlayBitmap ?: return
        val indices = IntArray(strokeTouchedCount)
        val colors = IntArray(strokeTouchedCount)
        val width = bmp.width
        for (i in 0 until strokeTouchedCount) {
            val idx = strokeTouchedIndices[i]
            indices[i] = idx
            val row = idx / width
            val col = idx % width
            colors[i] = bmp[col, row]
        }
        pixelCanvasUseCase.batchSetPixels(indices, colors, strokeTouchedCount)
    }

    private fun applyTouchedOverlayToMainBitmap() {
        val bmp = bitmapManager.bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        for (i in 0 until strokeTouchedCount) {
            val idx = strokeTouchedIndices[i]
            val row = idx / w
            val col = idx % w
            if (row in 0 until h && col in 0 until w) {
                bmp[col, row] = pixelCanvasUseCase.getCompositedPixelAt(row, col)
            }
        }
    }

    private fun applyStrokeUpdate(update: StrokeUpdate): Boolean {
        var applied = false
        update.mainLayerPixels?.let { pixels ->
            pixelCanvasUseCase.setAllPixels(pixels, ignoreSelection = true)
            val bmp = bitmapManager.bitmap
            val w = pixelCanvasUseCase.getCanvasWidth()
            val h = pixelCanvasUseCase.getCanvasHeight()
            if (bmp != null && bmp.width == w && bmp.height == h && pixels.size == w * h) {
                val composited = IntArray(w * h)
                for (r in 0 until h) {
                    val offset = r * w
                    for (c in 0 until w) {
                        composited[offset + c] = pixelCanvasUseCase.getCompositedPixelAt(r, c)
                    }
                }
                bmp.setPixels(composited, 0, w, 0, 0, w, h)
                bitmapManager.incrementDrawVersion()
            } else {
                scope.launch { bitmapManager.refreshBitmapState() }
            }
            applied = true
        }
        update.overlayPixels?.let {
            val bmp = bitmapManager.overlayBitmap ?: return@let
            val w = pixelCanvasUseCase.getCanvasWidth()
            val h = pixelCanvasUseCase.getCanvasHeight()
            if (it.isEmpty()) bitmapManager.clearOverlayBitmap()
            else bmp.setPixels(it, 0, w, 0, 0, w, h)
            bitmapManager.incrementOverlayVersion()
            applied = true
        }
        update.updatedSelectionMask?.let { mask ->
            pixelCanvasUseCase.setSelectionMask(mask)
            val w = pixelCanvasUseCase.getCanvasWidth()
            val h = pixelCanvasUseCase.getCanvasHeight()
            val sel = SelectionState(
                mask = mask,
                bounds = SelectionState.computeBounds(mask, w, h),
                canvasWidth = w,
                canvasHeight = h
            )
            bitmapManager.refreshSelectionBitmap(sel)
            onSelectionStateChanged(sel)
            applied = true
        }
        return applied
    }

    fun onStrokeStart(
        tool: StrokeTool,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        brushShape: BrushShape,
        selectionState: SelectionState?,
        zoomScale: Float = 1f
    ) {
        if (tool is PencilTool) tool.brushShape = brushShape
        if (tool is EraserTool) tool.brushShape = brushShape

        bitmapManager.ensureOverlayBitmap()
        if (!tool.commitsImmediately) {
            beginOverlayStrokeTracking()
        }

        if (tool is SelectionTool) {
            if (selectionState == null) {
                tool.clearSelection()
            }
            if (tool is RectangleSelectionTool) {
                tool.setZoomScale(zoomScale)
            }
        }

        val update = tool.beginStroke(
            canvas = pixelCanvasUseCase,
            row = row,
            col = col,
            color = color,
            scale = scale,
            plotPreviewPixel = { r, c, argb -> plotOverlayPixel(r, c, argb) },
            onCommittedPixel = { r, c -> applyCommittedPixelToMainBitmap(r, c) }
        )

        if (!applyStrokeUpdate(update)) {
            if (tool.commitsImmediately) {
                bitmapManager.incrementDrawVersion()
            } else {
                bitmapManager.incrementOverlayVersion()
                bitmapManager.incrementDrawVersion()
            }
        }
    }

    fun onStrokeMove(
        tool: StrokeTool,
        row: Int,
        col: Int
    ) {
        if (tool.commitsImmediately) {
            tool.updateStroke(
                canvas = pixelCanvasUseCase,
                row = row,
                col = col,
                plotPreviewPixel = { _, _, _ -> },
                onCommittedPixel = { r, c -> applyCommittedPixelToMainBitmap(r, c) }
            )
            bitmapManager.incrementDrawVersion()
            return
        }

        if (tool is ShapeTool || tool is SelectionTool) {
            beginOverlayStrokeTracking()
        }

        val update = tool.updateStroke(
            canvas = pixelCanvasUseCase,
            row = row,
            col = col,
            plotPreviewPixel = { r, c, argb -> plotOverlayPixel(r, c, argb) },
            onCommittedPixel = { _, _ -> }
        )

        if (!applyStrokeUpdate(update)) {
            bitmapManager.incrementOverlayVersion()
            bitmapManager.incrementDrawVersion()
        }
    }

    fun onStrokeEnd(
        tool: StrokeTool,
        isAppendSelectionMode: Boolean,
        currentSelectionState: SelectionState?
    ): StrokeEndResult {
        tool.endStroke()

        if (tool is SelectionTool) {
            val sel = tool.currentSelection
            var finalSel: SelectionState? = null

            if (sel != null) {
                finalSel = if (isAppendSelectionMode && currentSelectionState != null) {
                    val w = pixelCanvasUseCase.getCanvasWidth()
                    val h = pixelCanvasUseCase.getCanvasHeight()
                    val newMask = BooleanArray(w * h)
                    for (i in newMask.indices) {
                        newMask[i] = currentSelectionState.mask[i] || sel.mask[i]
                    }
                    val newBounds = android.graphics.Rect(
                        minOf(currentSelectionState.bounds.left, sel.bounds.left),
                        minOf(currentSelectionState.bounds.top, sel.bounds.top),
                        maxOf(currentSelectionState.bounds.right, sel.bounds.right),
                        maxOf(currentSelectionState.bounds.bottom, sel.bounds.bottom)
                    )
                    SelectionState(newMask, newBounds, w, h)
                } else {
                    sel
                }

                pixelCanvasUseCase.setSelectionMask(finalSel.mask)
                bitmapManager.refreshSelectionBitmap(finalSel)
            }
            restoreTouchedOverlayInMainBitmap()
            bitmapManager.clearOverlayBitmap()
            bitmapManager.incrementOverlayVersion()
            bitmapManager.incrementDrawVersion()
            strokeTouchedCount = 0

            return StrokeEndResult(updatedSelectionState = finalSel, shouldUpdateHistory = false)
        }

        if (!tool.commitsImmediately && strokeTouchedCount > 0) {
            commitOverlayStrokeToLayer()
            applyTouchedOverlayToMainBitmap()
            bitmapManager.clearOverlayBitmap()
            bitmapManager.incrementDrawVersion()
            bitmapManager.incrementOverlayVersion()
        }

        strokeTouchedCount = 0
        return StrokeEndResult(shouldUpdateHistory = !tool.staysPendingAfterStroke)
    }

    fun onStrokeCancel(tool: StrokeTool) {
        cancelPendingTool(tool)
    }

    fun cancelPendingTool(tool: StrokeTool): Boolean {
        val pendingUpdate = tool.cancelPending(pixelCanvasUseCase)
        restoreTouchedOverlayInMainBitmap()
        if (pendingUpdate != null) {
            applyStrokeUpdate(pendingUpdate)
            bitmapManager.clearOverlayBitmap()
            bitmapManager.incrementOverlayVersion()
            bitmapManager.incrementDrawVersion()
            return true
        } else {
            tool.cancelStroke()
        }
        bitmapManager.clearOverlayBitmap()
        bitmapManager.incrementOverlayVersion()
        bitmapManager.incrementDrawVersion()
        return false
    }

    fun commitPendingTool(tool: StrokeTool): Boolean {
        val update = tool.commitPending(pixelCanvasUseCase)
        if (update != null) {
            applyStrokeUpdate(update)
            return true
        }
        return false
    }
}
