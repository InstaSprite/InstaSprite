package com.instasprite.app.domain.draw

import com.instasprite.app.domain.canvashistory.CanvasHistoryManager
import com.instasprite.app.domain.canvashistory.HistoryCanvasState
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import com.instasprite.app.domain.canvashistory.OperationEntry
import com.instasprite.app.domain.canvashistory.TileChangeTracker
import com.instasprite.app.domain.canvashistory.TransformEntry
import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.ui.drawing.contract.PixelCanvasState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HistoryDelegate(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    historyDiskStore: HistoryDiskStore?,
    private val canvasState: MutableStateFlow<PixelCanvasState>,
    private val bitmapManager: BitmapManager,
    private val scope: CoroutineScope,
    private val refreshCanvasSizeState: () -> Unit,
    private val refreshLayerState: () -> Unit,
    private val refreshActiveLayerState: () -> Unit,
    private val syncStateVersions: () -> Unit
) : IHistoryManager {

    private val canvasHistoryManager = CanvasHistoryManager(historyDiskStore)
    private var activeHistoryTracker: TileChangeTracker? = null

    override fun saveState() {
        if (activeHistoryTracker == null) {
            val sel = canvasState.value.selectionState
            activeHistoryTracker = TileChangeTracker(sel?.deepCopy())
            pixelCanvasUseCase.beginTileHistory(activeHistoryTracker!!)
        }
    }

    override fun undo() {
        discardHistoryCapture()
        val restoredState = canvasHistoryManager.undo(captureHistoryCanvasState())
        if (restoredState != null) {
            applyHistoryCanvasState(restoredState)
            bitmapManager.clearOverlayBitmap()
            bitmapManager.incrementOverlayVersion()
            refreshCanvasSizeState()
            refreshLayerState()
            refreshActiveLayerState()
            scope.launch {
                bitmapManager.refreshBitmapState()
                syncStateVersions()
            }
        }
    }

    override fun redo() {
        discardHistoryCapture()
        val restoredState = canvasHistoryManager.redo(captureHistoryCanvasState())
        if (restoredState != null) {
            applyHistoryCanvasState(restoredState)
            bitmapManager.clearOverlayBitmap()
            bitmapManager.incrementOverlayVersion()
            refreshCanvasSizeState()
            refreshLayerState()
            refreshActiveLayerState()
            scope.launch {
                bitmapManager.refreshBitmapState()
                syncStateVersions()
            }
        }
    }

    override fun resetHistory() {
        canvasHistoryManager.reset()
        discardHistoryCapture()
    }

    override fun discardHistoryCapture() {
        if (activeHistoryTracker != null) {
            pixelCanvasUseCase.endTileHistory()
            activeHistoryTracker = null
        }
    }

    override fun updateHistoryCurrentState() {
        val tracker = activeHistoryTracker ?: return
        pixelCanvasUseCase.endTileHistory()
        activeHistoryTracker = null

        val sel = canvasState.value.selectionState
        val entry = tracker.buildUndoEntry(sel?.deepCopy())
        if (!entry.isEmpty()) {
            canvasHistoryManager.push(entry)
        }
    }

    override fun restorePendingHistoryCapture() {
        val tracker = activeHistoryTracker ?: return
        val sel = canvasState.value.selectionState
        val entry = tracker.buildUndoEntry(sel?.deepCopy())
        val restored = canvasHistoryManager.restore(entry, captureHistoryCanvasState())
        applyHistoryCanvasState(restored)
        refreshLayerState()
        discardHistoryCapture()
    }

    override fun recordOperationHistory(operation: () -> Unit) {
        discardHistoryCapture()
        val before = captureHistoryCanvasState()
        operation()
        val after = captureHistoryCanvasState()
        if (before != after) {
            canvasHistoryManager.push(OperationEntry(before = before, after = after))
        }
    }

    override fun recordTransformHistory(transform: TransformType, operation: () -> Unit) {
        discardHistoryCapture()
        operation()
        canvasHistoryManager.push(TransformEntry(transform))
    }

    private fun captureHistoryCanvasState(): HistoryCanvasState {
        return HistoryCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().map { it.copy() },
            activeLayerId = pixelCanvasUseCase.getActiveLayerId(),
            selectionState = canvasState.value.selectionState?.deepCopy()
        )
    }

    private fun applyHistoryCanvasState(state: HistoryCanvasState) {
        pixelCanvasUseCase.setCanvas(
            Sprite(
                width = state.width,
                height = state.height,
                layers = state.layers.map { it.copy() }
            )
        )
        pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
        pixelCanvasUseCase.setSelectionMask(state.selectionState?.mask)
        canvasState.value = canvasState.value.copy(selectionState = state.selectionState)
        bitmapManager.refreshSelectionBitmap(state.selectionState)
    }
}
