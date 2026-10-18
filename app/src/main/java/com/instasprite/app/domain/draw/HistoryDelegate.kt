package com.instasprite.app.domain.draw

import com.instasprite.app.domain.canvashistory.CanvasHistoryManager
import com.instasprite.app.domain.canvashistory.CelEditEntry
import com.instasprite.app.domain.canvashistory.HistoryCanvasState
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import com.instasprite.app.domain.canvashistory.OperationEntry
import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HistoryDelegate(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    historyDiskStore: HistoryDiskStore?,
    private val canvasState: MutableStateFlow<DrawingCanvasState>,
    private val bitmapManager: BitmapManager,
    private val scope: CoroutineScope,
    private val refreshCanvasSizeState: () -> Unit,
    private val refreshLayerState: () -> Unit,
    private val refreshActiveLayerState: () -> Unit,
    private val syncStateVersions: () -> Unit
) : IHistoryManager {

    private val canvasHistoryManager = CanvasHistoryManager(historyDiskStore)
    private var pendingState: HistoryCanvasState? = null

    override fun saveState() {
        if (pendingState == null) pendingState = captureHistoryCanvasState()
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
        pendingState = null
    }

    override fun updateHistoryCurrentState() {
        val before = pendingState ?: return
        pendingState = null
        val after = captureHistoryCanvasState()
        val oldAnimation = before.sprite?.animation ?: return
        val newAnimation = after.sprite?.animation ?: return
        val oldCel = oldAnimation.cel(before.activeLayerId, before.activeFrameId)
        val newCel = newAnimation.cel(before.activeLayerId, before.activeFrameId)
        val oldImage = oldCel?.let { oldAnimation.images[it.dataId] }
        val newImage = newCel?.let { newAnimation.images[it.dataId] }
        val changed = ((oldImage?.tiles?.keys ?: emptySet()) + (newImage?.tiles?.keys ?: emptySet())).filter { coord ->
            val old = oldImage?.tiles?.get(coord)
            val new = newImage?.tiles?.get(coord)
            if (old == null) new != null else new == null || !old.contentEquals(new)
        }.toSet()
        val entry = CelEditEntry(before.activeLayerId, before.activeFrameId, oldCel, newCel,
            oldImage?.let { CelImage(it.id, it.tiles.filterKeys { key -> key in changed }) },
            newImage?.let { CelImage(it.id, it.tiles.filterKeys { key -> key in changed }) },
            before.selectionState, after.selectionState)
        if (!entry.isEmpty()) canvasHistoryManager.push(entry)
    }

    override fun restorePendingHistoryCapture() {
        val before = pendingState ?: return
        pendingState = null
        applyHistoryCanvasState(before)
        refreshLayerState()
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
        recordOperationHistory(operation)
    }

    private fun captureHistoryCanvasState(): HistoryCanvasState {
        return HistoryCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().toList(),
            activeLayerId = pixelCanvasUseCase.getActiveLayerId(),
            selectionState = canvasState.value.selectionState?.deepCopy(),
            sprite = pixelCanvasUseCase.getSprite(),
            activeFrameId = pixelCanvasUseCase.activeFrameId
        )
    }

    private fun applyHistoryCanvasState(state: HistoryCanvasState) {
        pixelCanvasUseCase.setCanvas(
            state.sprite ?: Sprite(
                width = state.width,
                height = state.height,
                layers = state.layers.map { it.copy() }
            )
        )
        pixelCanvasUseCase.selectFrame(state.activeFrameId)
        pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
        pixelCanvasUseCase.setSelectionMask(state.selectionState?.mask)
        canvasState.value = canvasState.value.copy(selectionState = state.selectionState)
        bitmapManager.refreshSelectionBitmap(state.selectionState)
    }
}
