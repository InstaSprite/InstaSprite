package com.instasprite.app.domain.draw

import androidx.compose.ui.graphics.Color
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.draw.state.*
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.FillTool
import com.instasprite.app.domain.tool.MoveTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.selection.LassoSelectionTool
import com.instasprite.app.domain.tool.selection.MagicWandTool
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
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
    override val pixelCanvasUseCase: PixelCanvasUseCase,
    private val colorPaletteRepository: ColorPaletteRepository,
    private val scope: CoroutineScope,
    historyDiskStore: HistoryDiskStore? = null
) : ILayerManager, ITransformManager, IHistoryManager, InteractionContext {

    private var isProcessingTool = false

    override val bitmapManager = BitmapManager(pixelCanvasUseCase)
    override val strokeEngine = StrokeEngine(pixelCanvasUseCase, bitmapManager, scope) { sel ->
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
    override val mutableCanvasState: MutableStateFlow<PixelCanvasState> = _canvasState
    val canvasState: StateFlow<PixelCanvasState> = _canvasState.asStateFlow()

    override var selectedTool: Tool = PencilTool
    override var activeColor: Color = Color.Black
    override var toolSize: Int = 1
    override var brushShape: BrushShape = BrushShape.Circle
    override var isAppendSelectionMode: Boolean = false
    override var zoomScale: Float = 1f

    private var activeState: CanvasInteractionState = StandbyState

    override fun transitionTo(newState: CanvasInteractionState) {
        activeState = newState
    }

    override val historyManager = HistoryDelegate(
        pixelCanvasUseCase = pixelCanvasUseCase,
        historyDiskStore = historyDiskStore,
        canvasState = _canvasState,
        bitmapManager = bitmapManager,
        scope = scope,
        refreshCanvasSizeState = ::refreshCanvasSizeState,
        refreshLayerState = ::refreshLayerState,
        refreshActiveLayerState = ::refreshActiveLayerState,
        syncStateVersions = ::syncStateVersions
    )

    private val layerDelegate = LayerDelegate(
        pixelCanvasUseCase = pixelCanvasUseCase,
        historyManager = historyManager,
        refreshLayerState = ::refreshLayerState,
        refreshActiveLayerState = ::refreshActiveLayerState,
        syncStateVersions = ::syncStateVersions,
        scope = scope,
        bitmapManager = bitmapManager
    )

    private val transformDelegate = TransformDelegate(
        pixelCanvasUseCase = pixelCanvasUseCase,
        historyManager = historyManager,
        refreshCanvasSizeState = ::refreshCanvasSizeState,
        refreshLayerState = ::refreshLayerState,
        scope = scope,
        bitmapManager = bitmapManager,
        syncStateVersions = ::syncStateVersions
    )

    // =========== FSM ===========

    fun onStrokeStart(
        tool: StrokeTool,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        brushShape: BrushShape,
        zoomScale: Float
    ) {
        if (isProcessingTool) return
        this.selectedTool = tool
        this.activeColor = color
        this.toolSize = scale
        this.brushShape = brushShape
        this.zoomScale = zoomScale

        activeState.onTouchStart(row, col, this)
        syncStateVersions()
    }

    fun onStrokeMove(tool: StrokeTool, row: Int, col: Int) {
        this.selectedTool = tool
        activeState.onTouchMove(row, col, this)
        syncStateVersions()
    }

    fun onStrokeEnd(tool: StrokeTool, isAppendSelectionMode: Boolean) {
        this.selectedTool = tool
        this.isAppendSelectionMode = isAppendSelectionMode
        activeState.onTouchEnd(this)
        syncStateVersions()
    }

    fun onStrokeCancel(tool: StrokeTool) {
        this.selectedTool = tool
        activeState.onTouchCancel(this)
        syncStateVersions()
    }

    fun commitPendingTool(tool: StrokeTool) {
        this.selectedTool = tool
        activeState.commitPending(this)
        syncStateVersions()
    }

    fun cancelPendingTool(tool: StrokeTool): Boolean {
        this.selectedTool = tool
        activeState.cancelPending(this)
        syncStateVersions()
        return true
    }

    fun onTapAt(tool: Tool, row: Int, col: Int, color: Color, size: Int): TapResult {
        if (isProcessingTool) return TapResult.HandledSync

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
            isProcessingTool = true
            saveState()
            scope.launch {
                try {
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
                } finally {
                    isProcessingTool = false
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

    // =========== SELECTION ===========
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
        val newSel = SelectionState(
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

    // =========== LAYER ===========
    override fun addLayer(name: String) = layerDelegate.addLayer(name)
    override fun removeLayer(layerId: String) = layerDelegate.removeLayer(layerId)
    override fun selectLayer(layerId: String) = layerDelegate.selectLayer(layerId)
    override fun toggleLock(layerId: String) = layerDelegate.toggleLock(layerId)
    override fun toggleVisibility(layerId: String) = layerDelegate.toggleVisibility(layerId)
    override fun mergeLayerDown(layerId: String) = layerDelegate.mergeLayerDown(layerId)
    override fun reorderLayer(fromIndex: Int, toIndex: Int) = layerDelegate.reorderLayer(fromIndex, toIndex)
    override fun setLayerOpacity(layerId: String, opacity: Float) = layerDelegate.setLayerOpacity(layerId, opacity)
    override fun setLayerBlendMode(layerId: String, mode: BlendMode) = layerDelegate.setLayerBlendMode(layerId, mode)

    // =========== TRANSFORM ===========

    override fun rotate() = transformDelegate.rotate()
    override fun hFlip() = transformDelegate.hFlip()
    override fun vFlip() = transformDelegate.vFlip()
    override fun resizeCanvas(width: Int, height: Int) = transformDelegate.resizeCanvas(width, height)

    // =========== HISTORY ===========

    override fun saveState() = historyManager.saveState()
    override fun undo() = historyManager.undo()
    override fun redo() = historyManager.redo()
    override fun resetHistory() = historyManager.resetHistory()
    override fun discardHistoryCapture() = historyManager.discardHistoryCapture()
    override fun updateHistoryCurrentState() = historyManager.updateHistoryCurrentState()
    override fun restorePendingHistoryCapture() = historyManager.restorePendingHistoryCapture()
    override fun recordOperationHistory(operation: () -> Unit) = historyManager.recordOperationHistory(operation)
    override fun recordTransformHistory(transform: TransformType, operation: () -> Unit) = historyManager.recordTransformHistory(transform, operation)

    // =========== HELPER ===========

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
        MoveTool.reset()
        RectangleSelectionTool.clearSelection()
        LassoSelectionTool.clearSelection()
        MagicWandTool.clearSelection()
        activeState = StandbyState
    }
}
