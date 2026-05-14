package com.instasprite.app.ui.drawing

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.PixelCanvasRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.data.repository.StorageLocationRepository
import com.instasprite.app.domain.canvashistory.CanvasHistoryManager
import com.instasprite.app.domain.canvashistory.HistoryCanvasState
import com.instasprite.app.domain.canvashistory.OperationEntry
import com.instasprite.app.domain.canvashistory.TileChangeTracker
import com.instasprite.app.domain.canvashistory.TransformEntry
import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.tool.FillTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.ui.drawing.contract.CanvasMenuEvent
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteState
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import com.instasprite.app.ui.drawing.contract.PixelCanvasState
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.set
import androidx.core.graphics.get
import com.instasprite.app.data.model.DrawSetting
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.ui.drawing.contract.CursorDrawEvent
import com.instasprite.app.ui.drawing.contract.CursorState
import com.instasprite.app.utils.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext


data class DrawingScreenState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedTool: Tool,
    val toolSize: Int,
    val showLayerDrawer: Boolean = false,
    val isAppendSelectionMode: Boolean = false,
    val isCursorMode: Boolean = false,
    val cursorState: CursorState = CursorState()
)

@HiltViewModel(assistedFactory = DrawingViewModel.Factory::class)
class DrawingViewModel @AssistedInject constructor(
    @Assisted("spriteId") val spriteId: String,
    @Assisted("spriteWidth") private val width: Int,
    @Assisted("spriteHeight") private val height: Int,
    @Assisted("spriteName") private val spriteName: String?,
    private val storageLocationRepository: StorageLocationRepository,
    private val pixelCanvasRepository: PixelCanvasRepository,
    private val spriteDataRepository: SpriteDatabaseRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
    private val fileRepository: FileRepository,
    private val dialogController: DialogController<DrawingDialog>,
    @ApplicationContext private val applicationContext: Context
) : ViewModel(),
    DialogController<DrawingDialog> by dialogController {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("spriteId") spriteId: String,
            @Assisted("spriteWidth") width: Int,
            @Assisted("spriteHeight") height: Int,
            @Assisted("spriteName") spriteName: String?
        ): DrawingViewModel
    }

    private val canvasWidth: Int = if(width > 0) width else pixelCanvasRepository.width
    private val canvasHeight: Int = if(height > 0) height else pixelCanvasRepository.height
    private val canvasHistoryManager = CanvasHistoryManager()
    private val pixelCanvasUseCase = PixelCanvasUseCase(
        pixelCanvasRepository = pixelCanvasRepository,
        colorPaletteRepository = colorPaletteRepository
    )
    private var activeHistoryTracker: TileChangeTracker? = null

    // bitmap managed by ViewModel for incremental pixel updates
    private var _bitmap: Bitmap? = null
    val bitmap: Bitmap? get() = _bitmap
    private var _drawVersion: Long = 0

    // overlay bitmap for stroke preview
    private var _overlayBitmap: Bitmap? = null
    val overlayBitmap: Bitmap? get() = _overlayBitmap
    private var _overlayVersion: Long = 0

    // selection bitmap
    private var _selectionBitmap: Bitmap? = null
    val selectionBitmap: Bitmap? get() = _selectionBitmap
    private var _selectionVersion: Long = 0

    // Reused stroke buffers to avoid per-move allocations.
    private var strokeTouchMarks: IntArray = IntArray(0)
    private var strokeTouchedIndices: IntArray = IntArray(0)
    private var strokeTouchedCount: Int = 0
    private var strokeGeneration: Int = 1

    // Guard to prevent launching multiple concurrent fill operations
    @Volatile private var _fillRunning: Boolean = false

    private val _uiState = MutableStateFlow(
        DrawingScreenState(
            selectedTool = PencilTool,
            toolSize = 1,
        )
    )
    val uiState: StateFlow<DrawingScreenState> = _uiState.asStateFlow()

    private val _canvasState = MutableStateFlow(
        PixelCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().toList(),
            activeLayerId = pixelCanvasUseCase.getActiveLayerId()
        )
    )
    val canvasState: StateFlow<PixelCanvasState> = _canvasState.asStateFlow()

    private val _lastSavedLocation = MutableStateFlow<Uri?>(null)
    val lastSavedLocation: StateFlow<Uri?> = _lastSavedLocation.asStateFlow()

    var colorPalette = colorPaletteRepository.colors
    var activeColor = colorPaletteRepository.activeColor
    var recentColors = colorPaletteRepository.recentColors

    var onOpenPalette: () -> Unit = {}

    val colorPaletteState: StateFlow<ColorPaletteState> = combine(
        colorPaletteRepository.colors,
        colorPaletteRepository.activeColor,
        colorPaletteRepository.recentColors
    ) { palette, active, recent ->
        ColorPaletteState(
            colorPalette = palette,
            activeColor = active,
            recentColors = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ColorPaletteState(
            colorPalette = colorPalette.value,
            activeColor = activeColor.value,
            recentColors = recentColors.value
        )
    )

    init {
        setCanvasSize(canvasWidth, canvasHeight)

        applyDrawSetting(AppSettings.getDrawSetting(applicationContext))

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadFromDB()
            }
            refreshFullCanvasState()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _bitmap?.recycle()
        _bitmap = null
        _overlayBitmap?.recycle()
        _overlayBitmap = null
        _selectionBitmap?.recycle()
        _selectionBitmap = null
    }

    private fun applyDrawSetting(drawSetting: DrawSetting) {

        if (drawSetting.isCursorMode) {
            // Use toggle because cursor mode default to false
            toggleCursorMode(-1f, -1f)
        }
    }

    private fun ensureBitmap(width: Int, height: Int) {
        if (_bitmap == null || _bitmap!!.width != width || _bitmap!!.height != height) {
            _bitmap?.recycle()
            _bitmap = if (width > 0 && height > 0) createBitmap(width, height) else null
        }
    }

    private fun ensureOverlayBitmap() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (_overlayBitmap == null || _overlayBitmap!!.width != w || _overlayBitmap!!.height != h) {
            _overlayBitmap?.recycle()
            _overlayBitmap = if (w > 0 && h > 0) createBitmap(w, h) else null
        }
    }

    private fun clearOverlayBitmap() {
        _overlayBitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    }

    private fun ensureSelectionBitmap() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (_selectionBitmap == null || _selectionBitmap!!.width != w || _selectionBitmap!!.height != h) {
            _selectionBitmap?.recycle()
            _selectionBitmap = if (w > 0 && h > 0) createBitmap(w, h) else null
        }
    }
    
    private fun clearSelectionBitmap() {
        _selectionBitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
    }

    private fun refreshSelectionBitmap() {
        ensureSelectionBitmap()
        val sel = _canvasState.value.selectionState
        if (sel != null) {
            val bmp = _selectionBitmap ?: return
            val mask = sel.mask
            val w = bmp.width
            val h = bmp.height
            val dimTint = 0x60000000 // dim background
            val pixels = IntArray(w * h)
            
            for (i in 0 until w * h) {
                if (!mask[i]) {
                    pixels[i] = dimTint
                }
            }
            
            bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        } else {
            clearSelectionBitmap()
        }
        _selectionVersion++
    }

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

    private fun beginOverlayStrokeTracking() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        ensureStrokeTrackingCapacity(w, h)
        strokeTouchedCount = 0
        strokeGeneration += 1
        if (strokeGeneration == Int.MAX_VALUE) {
            strokeTouchMarks.fill(0)
            strokeGeneration = 1
        }
        clearOverlayBitmap()
    }

    private fun plotOverlayPixel(row: Int, col: Int, color: Int) {
        val bmp = _overlayBitmap ?: return
        if (row !in 0 until bmp.height || col !in 0 until bmp.width) return
        val index = row * bmp.width + col
        if (strokeTouchMarks[index] == strokeGeneration) return
        strokeTouchMarks[index] = strokeGeneration
        if (strokeTouchedCount >= strokeTouchedIndices.size) {
            strokeTouchedIndices = strokeTouchedIndices.copyOf(strokeTouchedIndices.size * 2)
        }
        strokeTouchedIndices[strokeTouchedCount++] = index
        bmp[col, row] = color
    }

    private fun applyCommittedPixelToMainBitmap(row: Int, col: Int) {
        val bmp = _bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (row !in 0 until h || col !in 0 until w) return
        val composited = pixelCanvasUseCase.getCompositedPixelAt(row, col)
        bmp[col, row] = composited
    }

    private fun commitOverlayStrokeToLayer() {
        if (strokeTouchedCount <= 0) return
        val bmp = _overlayBitmap ?: return
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
        val bmp = _bitmap ?: return
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

    fun onColorPaletteEvent(event: ColorPaletteEvent) {
        when (event) {
            is ColorPaletteEvent.SelectColor -> selectColor(event.color)
            is ColorPaletteEvent.OpenColorWheelDialog -> openDialog(DrawingDialog.ColorWheel)
        }
    }

    fun onCanvasMenuEvent(event: CanvasMenuEvent) {
        when (event) {
            is CanvasMenuEvent.RotateCanvas -> rotate()
            is CanvasMenuEvent.HorizontalFlip -> hFlip()
            is CanvasMenuEvent.VerticalFlip -> vFlip()
            is CanvasMenuEvent.OpenResizeDialog -> openDialog(DrawingDialog.ResizeCanvas)
        }
    }

    fun onLayerEvent(event: LayerEvent) {
        when (event) {
            is LayerEvent.AddLayer -> {
                recordOperationHistory {
                    pixelCanvasUseCase.addLayer("Layer ${pixelCanvasUseCase.getLayers().size + 1}")
                }
                refreshLayerState()
                refreshActiveLayerState()
            }
            is LayerEvent.DeleteLayer -> {
                recordOperationHistory {
                    pixelCanvasUseCase.removeLayer(event.layerId)
                }
                refreshLayerState()
                refreshActiveLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
            }
            is LayerEvent.SelectLayer -> {
                pixelCanvasUseCase.setActiveLayer(event.layerId)
                refreshActiveLayerState()
            }
            is LayerEvent.ToggleLock -> {
                recordOperationHistory {
                    pixelCanvasUseCase.toggleLock(event.layerId)
                }
                refreshLayerState()
            }
            is LayerEvent.ToggleVisibility -> {
                recordOperationHistory {
                    pixelCanvasUseCase.toggleVisibility(event.layerId)
                }
                refreshLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
            }
            is LayerEvent.MergeLayerDown -> {
                recordOperationHistory {
                    pixelCanvasUseCase.mergeLayerDown(event.layerId)
                }
                refreshLayerState()
                refreshActiveLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
            }
            is LayerEvent.ReorderLayer -> {
                recordOperationHistory {
                    pixelCanvasUseCase.reorderLayer(fromIndex = event.fromIndex, toIndex = event.toIndex)
                }

                refreshLayerState()

                viewModelScope.launch {
                    refreshBitmapState()
                }
            }
        }
    }

    fun onCanvasEvent(event: PixelCanvasEvent) {
        when (event) {
            is PixelCanvasEvent.OnStrokeStart -> onStrokeStart(event.y, event.x, event.zoomScale)
            is PixelCanvasEvent.OnStrokeMove -> onStrokeMove(event.y, event.x, event.zoomScale)
            is PixelCanvasEvent.OnStrokeEnd -> onStrokeEnd()
            is PixelCanvasEvent.OnStrokeCancel -> onStrokeCancel()
            is PixelCanvasEvent.OnTapAt -> onTapAt(event.y, event.x)
            is PixelCanvasEvent.ClearSelection -> clearSelection()
            is PixelCanvasEvent.InvertSelection -> invertSelection()
        }
    }

    private fun clearSelection() {
        pixelCanvasUseCase.setSelectionMask(null)
        _canvasState.value = _canvasState.value.copy(selectionState = null)
        val tool = _uiState.value.selectedTool
        if (tool is SelectionTool) {
            tool.clearSelection()
        }
        refreshSelectionBitmap()
    }
    
    private fun invertSelection() {
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
        refreshSelectionBitmap()
    }

    fun onToolSelectorEvent(event: ToolSelectorEvent) {
        when (event) {
            is ToolSelectorEvent.Undo -> undo()
            is ToolSelectorEvent.Redo -> redo()
            is ToolSelectorEvent.OpenSaveImageDialog -> openDialog(DrawingDialog.SaveImage)
            is ToolSelectorEvent.OpenSaveISpriteDialog -> openDialog(DrawingDialog.SaveISprite)
            is ToolSelectorEvent.OpenLoadISpriteDialog -> openDialog(DrawingDialog.LoadISprite)
            is ToolSelectorEvent.SelectTool -> selectTool(tool = event.tool)
            is ToolSelectorEvent.ToggleAppendSelectionMode -> {
                _uiState.value = _uiState.value.copy(
                    isAppendSelectionMode = !_uiState.value.isAppendSelectionMode
                )
            }
        }
    }

    fun setCanvasSize(width: Int, height: Int) {
        pixelCanvasUseCase.setCanvas(width, height)
        _canvasState.value = _canvasState.value.copy(width = width, height = height)
    }

    fun selectColor(color: Color) {
        colorPaletteRepository.setActiveColor(color)
    }

    fun selectTool(tool: Tool) {
        _uiState.value = _uiState.value.copy(selectedTool = tool)
    }

    fun setToolSize(size: Int) {
        _uiState.value = _uiState.value.copy(toolSize = size)
    }

    fun toggleLayerDrawer() {
        _uiState.value = _uiState.value.copy(showLayerDrawer = !_uiState.value.showLayerDrawer)
    }

    fun onCursorDrawEvent(event: CursorDrawEvent) {
        when (event) {
            is CursorDrawEvent.ToggleCursorMode -> toggleCursorMode(event.cursorX, event.cursorY)
            is CursorDrawEvent.MoveCursor -> moveCursor(event.cursorX, event.cursorY)
            is CursorDrawEvent.DrawButtonPressed -> onCursorDrawPressed()
            is CursorDrawEvent.DrawButtonReleased -> onCursorDrawReleased()
        }
    }

    private fun toggleCursorMode(cx: Float, cy: Float) {
        val current = _uiState.value
        val newCursorMode = !current.isCursorMode
        val cursorState = if (newCursorMode) {
            val startX = if (cx >= 0f) cx else (canvasWidth / 2f)
            val startY = if (cy >= 0f) cy else (canvasHeight / 2f)
            CursorState(
                cursorX = startX.coerceIn(0f, canvasWidth.toFloat() - 0.01f),
                cursorY = startY.coerceIn(0f, canvasHeight.toFloat() - 0.01f),
                isVisible = true
            )
        } else {
            CursorState()
        }
        _uiState.value = current.copy(
            isCursorMode = newCursorMode,
            cursorState = cursorState
        )

        AppSettings.setCursorMode(applicationContext, newCursorMode)
    }

    private fun moveCursor(cursorX: Float, cursorY: Float) {
        val maxX = canvasWidth.toFloat() - 0.01f
        val maxY = canvasHeight.toFloat() - 0.01f
        val clampedX = cursorX.coerceIn(0f, maxX)
        val clampedY = cursorY.coerceIn(0f, maxY)
        val current = _uiState.value.cursorState

        val prevGridX = current.gridX
        val prevGridY = current.gridY

        _uiState.value = _uiState.value.copy(
            cursorState = current.copy(cursorX = clampedX, cursorY = clampedY, isVisible = true)
        )

        val newGridX = clampedX.toInt()
        val newGridY = clampedY.toInt()

        if (current.isDrawing && (newGridX != prevGridX || newGridY != prevGridY)) {
            val tool = _uiState.value.selectedTool
            if (tool is StrokeTool) {
                onStrokeMove(newGridY, newGridX)
            }
        }
    }

    private fun onCursorDrawPressed() {
        val cursor = _uiState.value.cursorState
        _uiState.value = _uiState.value.copy(
            cursorState = cursor.copy(isDrawing = true)
        )
        val tool = _uiState.value.selectedTool
        if (tool is StrokeTool) {
            onStrokeStart(cursor.gridY, cursor.gridX)
        } else {
            onTapAt(cursor.gridY, cursor.gridX)
        }
    }

    private fun onCursorDrawReleased() {
        val cursor = _uiState.value.cursorState
        _uiState.value = _uiState.value.copy(
            cursorState = cursor.copy(isDrawing = false)
        )
        val tool = _uiState.value.selectedTool
        if (tool is StrokeTool) {
            onStrokeEnd()
        }
    }

    // --- Stroke lifecycle ---

    private fun onStrokeStart(row: Int, col: Int, zoomScale: Float = 1f) {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        if (tool !is SelectionTool) {
            saveState()
        }

        val color = activeColor.value
        val scale = _uiState.value.toolSize
        ensureOverlayBitmap()
        if (!tool.commitsImmediately) {
            beginOverlayStrokeTracking()
        }

        if (tool is SelectionTool) {
            if (_canvasState.value.selectionState == null) {
                tool.clearSelection()
            }
            if (tool is com.instasprite.app.domain.tool.selection.RectangleSelectionTool) {
                tool.setZoomScale(zoomScale)
            }
        }

        tool.beginStroke(
            canvas = pixelCanvasUseCase,
            row = row,
            col = col,
            color = color,
            scale = scale,
            plotPreviewPixel = { r, c, argb -> plotOverlayPixel(r, c, argb) },
            onCommittedPixel = { r, c -> applyCommittedPixelToMainBitmap(r, c) }
        )

        if (tool.commitsImmediately) {
            _drawVersion++
            _canvasState.value = _canvasState.value.copy(drawVersion = _drawVersion)
        } else {
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(overlayVersion = _overlayVersion)
        }
    }

    private fun onStrokeMove(row: Int, col: Int, zoomScale: Float = 1f) {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        if (tool.commitsImmediately) {
            tool.updateStroke(
                canvas = pixelCanvasUseCase,
                row = row,
                col = col,
                plotPreviewPixel = { _, _, _ -> },
                onCommittedPixel = { r, c -> applyCommittedPixelToMainBitmap(r, c) }
            )
            _drawVersion++
            _canvasState.value = _canvasState.value.copy(drawVersion = _drawVersion)
            return
        }

        if (tool is ShapeTool || tool is SelectionTool) {
            beginOverlayStrokeTracking()
        }

        tool.updateStroke(
            canvas = pixelCanvasUseCase,
            row = row,
            col = col,
            plotPreviewPixel = { r, c, argb -> plotOverlayPixel(r, c, argb) },
            onCommittedPixel = { _, _ -> }
        )

        _overlayVersion++
        _canvasState.value = _canvasState.value.copy(overlayVersion = _overlayVersion)
    }

    private fun onStrokeEnd() {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        tool.endStroke()

        if (tool is SelectionTool) {
            val sel = tool.currentSelection
            if (sel != null) {
                val oldSel = _canvasState.value.selectionState
                val finalSel = if (_uiState.value.isAppendSelectionMode && oldSel != null) {
                    val w = pixelCanvasUseCase.getCanvasWidth()
                    val h = pixelCanvasUseCase.getCanvasHeight()
                    val newMask = BooleanArray(w * h)
                    for (i in newMask.indices) {
                        newMask[i] = oldSel.mask[i] || sel.mask[i]
                    }
                    val newBounds = android.graphics.Rect(
                        minOf(oldSel.bounds.left, sel.bounds.left),
                        minOf(oldSel.bounds.top, sel.bounds.top),
                        maxOf(oldSel.bounds.right, sel.bounds.right),
                        maxOf(oldSel.bounds.bottom, sel.bounds.bottom)
                    )
                    com.instasprite.app.domain.model.SelectionState(newMask, newBounds, w, h)
                } else {
                    sel
                }
                
                pixelCanvasUseCase.setSelectionMask(finalSel.mask)
                _canvasState.value = _canvasState.value.copy(selectionState = finalSel)
                refreshSelectionBitmap()
            }
            clearOverlayBitmap()
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(overlayVersion = _overlayVersion)
            strokeTouchedCount = 0
            return
        }

        if (!tool.commitsImmediately && strokeTouchedCount > 0) {
            commitOverlayStrokeToLayer()
            applyTouchedOverlayToMainBitmap()
            clearOverlayBitmap()
            _drawVersion++
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(
                drawVersion = _drawVersion,
                overlayVersion = _overlayVersion
            )
        }

        strokeTouchedCount = 0

        updateHistoryCurrentState()
        refreshLayerState()
    }

    private fun onStrokeCancel() {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        tool.cancelStroke()
        clearOverlayBitmap()
        _overlayVersion++

        if (tool !is SelectionTool) {
            restorePendingHistoryCapture()
            refreshLayerState()
            viewModelScope.launch {
                refreshBitmapState()
            }
        }
    }

    private fun onTapAt(row: Int, col: Int) {
        val tool = _uiState.value.selectedTool
        val color = activeColor.value
        val size = _uiState.value.toolSize

        if (tool is SelectionTool) {
            tool.apply(pixelCanvasUseCase, row, col, color)
            val sel = tool.currentSelection
            if (sel != null) {
                pixelCanvasUseCase.setSelectionMask(sel.mask)
                _canvasState.value = _canvasState.value.copy(selectionState = sel)
                refreshSelectionBitmap()
            }
            return
        }

        if (tool is FillTool) {
            if (_fillRunning) return
            _fillRunning = true

            saveState()

            viewModelScope.launch {
                val result = withContext(Dispatchers.Default) {
                    FillTool.fillDirect(pixelCanvasUseCase, row, col, color)
                }

                _fillRunning = false

                if (result != null) {
                    refreshBitmapRegion(
                        result.dirtyMinRow, result.dirtyMinCol,
                        result.dirtyMaxRow, result.dirtyMaxCol
                    )
                    _drawVersion++
                    _canvasState.value = _canvasState.value.copy(
                        drawVersion = _drawVersion,
                        layers = pixelCanvasUseCase.getLayers().toList()
                    )
                    updateHistoryCurrentState()
                } else {
                    discardHistoryCapture()
                }
            }
        } else {
            saveState()
            tool.apply(pixelCanvasUseCase, row, col, color, size)
            refreshLayerState()
            viewModelScope.launch {
                refreshBitmapState()
            }
            updateHistoryCurrentState()
        }
    }


    fun saveState() {
        if (activeHistoryTracker == null) {
            activeHistoryTracker = TileChangeTracker()
            pixelCanvasUseCase.beginTileHistory(activeHistoryTracker!!)
        }
    }

    private fun updateHistoryCurrentState() {
        val tracker = activeHistoryTracker ?: return
        pixelCanvasUseCase.endTileHistory()
        activeHistoryTracker = null

        val entry = tracker.buildUndoEntry()
        if (entry.deltas.isNotEmpty()) {
            canvasHistoryManager.push(entry)
        }
    }

    fun undo() {
        discardHistoryCapture()

        val restoredState = canvasHistoryManager.undo(captureHistoryCanvasState())
        if (restoredState != null) {
            applyHistoryCanvasState(restoredState)

            clearOverlayBitmap()
            refreshCanvasSizeState()
            refreshLayerState()
            refreshActiveLayerState()
            viewModelScope.launch {
                refreshBitmapState()
            }
        }
    }

    fun redo() {
        discardHistoryCapture()

        val restoredState = canvasHistoryManager.redo(captureHistoryCanvasState())
        if (restoredState != null) {
            applyHistoryCanvasState(restoredState)

            clearOverlayBitmap()
            refreshCanvasSizeState()
            refreshLayerState()
            refreshActiveLayerState()
            viewModelScope.launch {
                refreshBitmapState()
            }
        }
    }

    fun rotate() {
        recordTransformHistory(TransformType.ROTATE_CW) {
            pixelCanvasUseCase.rotateCanvas()
        }
        refreshCanvasSizeState()
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
    }

    fun hFlip() {
        recordTransformHistory(TransformType.FLIP_H) {
            pixelCanvasUseCase.hFlipCanvas()
        }
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
    }

    fun vFlip() {
        recordTransformHistory(TransformType.FLIP_V) {
            pixelCanvasUseCase.vFlipCanvas()
        }
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
    }

    fun resizeCanvas(width: Int, height: Int) {
        recordOperationHistory {
            pixelCanvasUseCase.resizeCanvas(width, height)
        }
        refreshCanvasSizeState()
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
    }

    private fun captureHistoryCanvasState(): HistoryCanvasState {
        return HistoryCanvasState(
            width = pixelCanvasUseCase.getCanvasWidth(),
            height = pixelCanvasUseCase.getCanvasHeight(),
            layers = pixelCanvasUseCase.getLayers().map { it.copy() },
            activeLayerId = pixelCanvasUseCase.getActiveLayerId()
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
        val entry = tracker.buildUndoEntry()
        val restored = canvasHistoryManager.restore(entry, captureHistoryCanvasState())
        applyHistoryCanvasState(restored)
        discardHistoryCapture()
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

    private suspend fun refreshBitmapState() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        
        val pixels = withContext(Dispatchers.Default) {
            pixelCanvasUseCase.getAllPixels()
        }

        ensureBitmap(w, h)
        if (pixels.size == w * h) {
            _bitmap?.setPixels(pixels, 0, w, 0, 0, w, h)
        }
        _drawVersion++

        _canvasState.value = _canvasState.value.copy(
            drawVersion = _drawVersion
        )
    }

    private suspend fun refreshFullCanvasState() {
        refreshCanvasSizeState()
        refreshLayerState()
        refreshActiveLayerState()
        refreshBitmapState()
    }

    private fun refreshBitmapRegion(startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        val bmp = _bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()

        val r0 = startRow.coerceAtLeast(0)
        val c0 = startCol.coerceAtLeast(0)
        val r1 = endRow.coerceAtMost(h - 1)
        val c1 = endCol.coerceAtMost(w - 1)

        val regionW = c1 - c0 + 1
        val regionH = r1 - r0 + 1
        if (regionW <= 0 || regionH <= 0) return

        val regionPixels = pixelCanvasUseCase.getAllPixelsInRegion(r0, c0, regionH, regionW)
        bmp.setPixels(regionPixels, 0, regionW, c0, r0, regionW, regionH)
    }


    suspend fun getLastSavedLocation(): Uri? {
        _lastSavedLocation.value = storageLocationRepository.getLastSavedLocation()
        return _lastSavedLocation.value
    }

    fun setLastSavedLocation(uri: Uri) {
        _lastSavedLocation.value = uri
        viewModelScope.launch {
            storageLocationRepository.setLastSavedLocation(uri)
        }
    }

    fun saveImage(
        folderUri: Uri,
        fileName: String,
        scalePercent: Int = 100
    ): Boolean {
        if (fileName.isBlank()) return false
        val sprite = pixelCanvasUseCase.getSprite()
        val bitmap = ImageExporter.convertToBitmap(
            sprite.compositedPixels,
            sprite.width,
            sprite.height,
            scalePercent
        ) ?: return false

        return fileRepository.saveFile(bitmap, folderUri, fileName)
    }

    suspend fun saveISprite(
        folderUri: Uri,
        fileName: String
    ): Boolean {
        if (fileName.isBlank()) return false
        if (_uiState.value.isSaving) return false

        _uiState.value = _uiState.value.copy(isSaving = true)
        return try {
            withContext(Dispatchers.IO) {
                fileRepository.saveISpriteFile(
                    pixelCanvasUseCase.getSprite(),
                    folderUri,
                    fileName
                )
            }
        } finally {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun getSpriteDataFromFile(fileUri: Uri): Sprite? {
        return fileRepository.loadISpriteFile(fileUri)
    }

    suspend fun loadSprite(sprite: Sprite) {
        setCanvasSize(sprite.width, sprite.height)
        pixelCanvasUseCase.setCanvas(sprite)
        sprite.colorPalette?.let {
            colorPaletteRepository.updatePalette(sprite.colorPalette.map { Color(it) })
        }
        canvasHistoryManager.reset()
        discardHistoryCapture()

        refreshFullCanvasState()
    }

    suspend fun saveToDB(spriteName: String? = null) {
        if (_uiState.value.isSaving) return

        _uiState.value = _uiState.value.copy(isSaving = true)
        try {
            withContext(Dispatchers.IO) {
                val sprite = pixelCanvasUseCase.getSprite()
                spriteDataRepository.saveSprite(sprite.copy(id = spriteId))
                spriteName?.let {
                    spriteDataRepository.changeName(spriteId, it)
                }
            }
        } finally {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    suspend fun loadFromDB() {
        val sprite = spriteDataRepository.loadSprite(spriteId)
        if (sprite != null) {
            loadSprite(sprite)
        } else {
            saveToDB(spriteName)
        }
    }

    fun updateColorPalette(colors: List<Color>) {
        if (colors.isNotEmpty()) {
            colorPaletteRepository.updatePalette(colors)
        }
    }
}
