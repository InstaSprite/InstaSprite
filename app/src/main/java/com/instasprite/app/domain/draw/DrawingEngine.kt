package com.instasprite.app.domain.draw

import androidx.compose.ui.graphics.Color
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.canvashistory.CanvasHistoryManager
import com.instasprite.app.domain.canvashistory.HistoryCanvasState
import com.instasprite.app.domain.canvashistory.OperationEntry
import com.instasprite.app.domain.canvashistory.TileChangeTracker
import com.instasprite.app.domain.canvashistory.TransformEntry
import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.FillTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.ui.drawing.contract.PixelCanvasState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TapResult {
    object FillStarted : TapResult()
    object HandledSync : TapResult()
}

class DrawingEngine(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    private val colorPaletteRepository: ColorPaletteRepository,
    private val scope: CoroutineScope
) {
    private val canvasHistoryManager = CanvasHistoryManager()
    private var activeHistoryTracker: TileChangeTracker? = null

    val bitmapManager = BitmapManager(pixelCanvasUseCase)
    private val strokeEngine = StrokeEngine(pixelCanvasUseCase, bitmapManager, scope) { sel ->
        _canvasState.value = _canvasState.value.copy(selectionState = sel)
    }

    private val _canvasState = MutableStateFlow(
        PixelCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().toList(),
            activeLayerId = pixelCanvasUseCase.getActiveLayerId()
        )
    )
    val canvasState: StateFlow<PixelCanvasState> = _canvasState.asStateFlow()

    // Stroke
    fun onStrokeStart(
        tool: StrokeTool,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        brushShape: BrushShape,
        zoomScale: Float
    ) {
        if (tool !is SelectionTool) {
            saveState()
        }
        val selectionState = _canvasState.value.selectionState
        strokeEngine.onStrokeStart(
            tool,
            row,
            col,
            color,
            scale,
            brushShape,
            selectionState,
            zoomScale
        )
        syncStateVersions()
    }

    fun onStrokeMove(tool: StrokeTool, row: Int, col: Int) {
        strokeEngine.onStrokeMove(tool, row, col)
        syncStateVersions()
    }

    fun onStrokeEnd(tool: StrokeTool, isAppendSelectionMode: Boolean) {
        val currentSelectionState = _canvasState.value.selectionState
        val result = strokeEngine.onStrokeEnd(tool, isAppendSelectionMode, currentSelectionState)

        if (result.updatedSelectionState != null) {
            _canvasState.value =
                _canvasState.value.copy(selectionState = result.updatedSelectionState)
        }

        if (result.shouldUpdateHistory) {
            updateHistoryCurrentState()
            refreshLayerState()
        }

        syncStateVersions()
    }

    fun onStrokeCancel(tool: StrokeTool) {
        strokeEngine.onStrokeCancel(tool)

        if (tool !is SelectionTool) {
            restorePendingHistoryCapture()
            refreshLayerState()
            scope.launch {
                bitmapManager.refreshBitmapState()
                syncStateVersions()
            }
        }
        syncStateVersions()
    }

    fun commitPendingTool(tool: StrokeTool) {
        if (strokeEngine.commitPendingTool(tool)) {
            updateHistoryCurrentState()
            syncStateVersions()
        }
    }

    fun cancelPendingTool(tool: StrokeTool): Boolean {
        if (strokeEngine.cancelPendingTool(tool)) {
            discardHistoryCapture()
            syncStateVersions()
            return true
        }
        return false
    }

    // Tap
    fun onTapAt(tool: Tool, row: Int, col: Int, color: Color, size: Int): TapResult {
        if (tool is SelectionTool) {
            tool.apply(pixelCanvasUseCase, row, col, color)
            val sel = tool.currentSelection
            if (sel != null) {
                pixelCanvasUseCase.setSelectionMask(sel.mask)
                _canvasState.value = _canvasState.value.copy(selectionState = sel)
                bitmapManager.refreshSelectionBitmap(sel)
                syncStateVersions()
            }
            return TapResult.HandledSync
        }

        if (tool is FillTool) {
            saveState()
            scope.launch {
                val result = withContext(Dispatchers.Default) {
                    FillTool.fillDirect(pixelCanvasUseCase, row, col, color)
                }

                if (result != null) {
                    bitmapManager.refreshBitmapRegion(
                        result.dirtyMinRow, result.dirtyMinCol,
                        result.dirtyMaxRow, result.dirtyMaxCol
                    )
                    _canvasState.value = _canvasState.value.copy(
                        drawVersion = bitmapManager.drawVersion,
                        layers = pixelCanvasUseCase.getLayers().toList()
                    )
                    updateHistoryCurrentState()
                } else {
                    discardHistoryCapture()
                }
            }
            return TapResult.FillStarted
        }

        saveState()
        tool.apply(pixelCanvasUseCase, row, col, color, size)
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
        updateHistoryCurrentState()
        return TapResult.HandledSync
    }

    // Selection

    fun clearSelection(tool: Tool?) {
        pixelCanvasUseCase.setSelectionMask(null)
        _canvasState.value = _canvasState.value.copy(selectionState = null)
        if (tool is SelectionTool) {
            tool.clearSelection()
        }
        bitmapManager.refreshSelectionBitmap(null)
        syncStateVersions()
    }

    fun invertSelection() {
        val sel = _canvasState.value.selectionState ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        val newMask = BooleanArray(w * h)
        for (i in newMask.indices) {
            newMask[i] = !sel.mask[i]
        }
        val newSel = com.instasprite.app.domain.model.SelectionState(
            mask = newMask,
            bounds = android.graphics.Rect(0, 0, w, h),
            canvasWidth = w,
            canvasHeight = h
        )
        pixelCanvasUseCase.setSelectionMask(newMask)
        _canvasState.value = _canvasState.value.copy(selectionState = newSel)
        bitmapManager.refreshSelectionBitmap(newSel)
        syncStateVersions()
    }

    // Layer stuffs

    fun addLayer(name: String) {
        recordOperationHistory {
            pixelCanvasUseCase.addLayer(name)
        }
        refreshLayerState()
        refreshActiveLayerState()
    }

    fun removeLayer(layerId: String) {
        recordOperationHistory {
            pixelCanvasUseCase.removeLayer(layerId)
        }
        refreshLayerState()
        refreshActiveLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun selectLayer(layerId: String) {
        pixelCanvasUseCase.setActiveLayer(layerId)
        refreshActiveLayerState()
    }

    fun toggleLock(layerId: String) {
        recordOperationHistory {
            pixelCanvasUseCase.toggleLock(layerId)
        }
        refreshLayerState()
    }

    fun toggleVisibility(layerId: String) {
        recordOperationHistory {
            pixelCanvasUseCase.toggleVisibility(layerId)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun mergeLayerDown(layerId: String) {
        recordOperationHistory {
            pixelCanvasUseCase.mergeLayerDown(layerId)
        }
        refreshLayerState()
        refreshActiveLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun reorderLayer(fromIndex: Int, toIndex: Int) {
        recordOperationHistory {
            pixelCanvasUseCase.reorderLayer(fromIndex = fromIndex, toIndex = toIndex)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        recordOperationHistory {
            pixelCanvasUseCase.setLayerOpacity(layerId, opacity)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun setLayerBlendMode(layerId: String, mode: com.instasprite.app.domain.model.BlendMode) {
        recordOperationHistory {
            pixelCanvasUseCase.setLayerBlendMode(layerId, mode)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    // Transform

    fun rotate() {
        recordTransformHistory(TransformType.ROTATE_CW) {
            pixelCanvasUseCase.rotateCanvas()
        }
        refreshCanvasSizeState()
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun hFlip() {
        recordTransformHistory(TransformType.FLIP_H) {
            pixelCanvasUseCase.hFlipCanvas()
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun vFlip() {
        recordTransformHistory(TransformType.FLIP_V) {
            pixelCanvasUseCase.vFlipCanvas()
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    fun resizeCanvas(width: Int, height: Int) {
        recordOperationHistory {
            pixelCanvasUseCase.resizeCanvas(width, height)
        }
        refreshCanvasSizeState()
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    // History

    fun saveState() {
        if (activeHistoryTracker == null) {
            val sel = _canvasState.value.selectionState
            activeHistoryTracker = TileChangeTracker(sel?.deepCopy())
            pixelCanvasUseCase.beginTileHistory(activeHistoryTracker!!)
        }
    }

    fun undo() {
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

    fun redo() {
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

    fun resetHistory() {
        canvasHistoryManager.reset()
        discardHistoryCapture()
    }

    private fun updateHistoryCurrentState() {
        val tracker = activeHistoryTracker ?: return
        pixelCanvasUseCase.endTileHistory()
        activeHistoryTracker = null

        val sel = _canvasState.value.selectionState
        val entry = tracker.buildUndoEntry(sel?.deepCopy())
        if (!entry.isEmpty()) {
            canvasHistoryManager.push(entry)
        }
    }

    private fun captureHistoryCanvasState(): HistoryCanvasState {
        return HistoryCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().map { it.copy() },
            activeLayerId = pixelCanvasUseCase.getActiveLayerId(),
            selectionState = _canvasState.value.selectionState?.deepCopy()
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
        _canvasState.value = _canvasState.value.copy(selectionState = state.selectionState)
        bitmapManager.refreshSelectionBitmap(state.selectionState)
    }

    private inline fun recordOperationHistory(operation: () -> Unit) {
        discardHistoryCapture()
        val before = captureHistoryCanvasState()
        operation()
        val after = captureHistoryCanvasState()
        if (before != after) {
            canvasHistoryManager.push(OperationEntry(before = before, after = after))
        }
    }

    private inline fun recordTransformHistory(transform: TransformType, operation: () -> Unit) {
        discardHistoryCapture()
        operation()
        canvasHistoryManager.push(TransformEntry(transform))
    }

    private fun discardHistoryCapture() {
        if (activeHistoryTracker != null) {
            pixelCanvasUseCase.endTileHistory()
            activeHistoryTracker = null
        }
    }

    private fun restorePendingHistoryCapture() {
        val tracker = activeHistoryTracker ?: return
        val sel = _canvasState.value.selectionState
        val entry = tracker.buildUndoEntry(sel?.deepCopy())
        val restored = canvasHistoryManager.restore(entry, captureHistoryCanvasState())
        applyHistoryCanvasState(restored)
        discardHistoryCapture()
    }

    // --- State Refresh ---

    fun setCanvasSize(width: Int, height: Int) {
        pixelCanvasUseCase.setCanvas(width, height)
        _canvasState.value = _canvasState.value.copy(width = width, height = height)
    }

    fun setCanvas(sprite: Sprite) {
        pixelCanvasUseCase.setCanvas(sprite)
    }

    suspend fun refreshFullCanvasState() {
        refreshCanvasSizeState()
        refreshLayerState()
        refreshActiveLayerState()
        bitmapManager.refreshBitmapState()
        syncStateVersions()
    }

    private fun refreshLayerState() {
        _canvasState.value = _canvasState.value.copy(
            layers = pixelCanvasUseCase.getLayers().toList()
        )
    }

    private fun refreshActiveLayerState() {
        _canvasState.value = _canvasState.value.copy(
            activeLayerId = pixelCanvasUseCase.getActiveLayerId()
        )
    }

    private fun refreshCanvasSizeState() {
        _canvasState.value = _canvasState.value.copy(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight()
        )
    }

    private fun syncStateVersions() {
        _canvasState.value = _canvasState.value.copy(
            drawVersion = bitmapManager.drawVersion,
            overlayVersion = bitmapManager.overlayVersion
        )
    }

    fun release() {
        bitmapManager.release()
    }
}
