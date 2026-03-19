package com.olaz.instasprite.ui.drawing

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.data.repository.PixelCanvasRepository
import com.olaz.instasprite.data.repository.SpriteDatabaseRepository
import com.olaz.instasprite.data.repository.StorageLocationRepository
import com.olaz.instasprite.domain.canvashistory.CanvasHistoryManager
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.tool.FillTool
import com.olaz.instasprite.domain.tool.PixelChange
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.StrokeTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.data.repository.FileRepository
import com.olaz.instasprite.domain.export.ImageExporter
import com.olaz.instasprite.ui.drawing.contract.CanvasMenuEvent
import com.olaz.instasprite.ui.drawing.contract.LayerEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteState
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.drawing.contract.ToolSelectorEvent
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


data class DrawingScreenState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedTool: Tool,
    val toolSize: Int,
    val showLayerDrawer: Boolean = false
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
    private val dialogController: DialogController<DrawingDialog>
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
    private val canvasHistoryManager = CanvasHistoryManager<PixelCanvasState>()
    private val pixelCanvasUseCase = PixelCanvasUseCase(
        pixelCanvasRepository = pixelCanvasRepository,
        colorPaletteRepository = colorPaletteRepository
    )

    // bitmap managed by ViewModel for incremental pixel updates
    private var _bitmap: Bitmap? = null
    val bitmap: Bitmap? get() = _bitmap
    private var _drawVersion: Long = 0

    // overlay bitmap for stroke preview
    private var _overlayBitmap: Bitmap? = null
    val overlayBitmap: Bitmap? get() = _overlayBitmap
    private var _overlayVersion: Long = 0

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

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadFromDB()
            }
            refreshFullCanvasState()
            _uiState.value = _uiState.value.copy(isLoading = false)
            saveState()
        }
    }

    override fun onCleared() {
        super.onCleared()
        _bitmap?.recycle()
        _bitmap = null
        _overlayBitmap?.recycle()
        _overlayBitmap = null
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

    private fun applyChangesToOverlay(changes: List<PixelChange>) {
        ensureOverlayBitmap()
        val bmp = _overlayBitmap ?: return
        val visibleChanges = pixelCanvasUseCase.filterVisibleChanges(changes)
        for (change in visibleChanges) {
            if (change.row in 0 until bmp.height && change.col in 0 until bmp.width) {
                bmp[change.col, change.row] = change.color
            }
        }
    }

    private fun applyChangesToMainBitmap(changes: List<PixelChange>) {
        val bmp = _bitmap ?: return
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        for (change in changes) {
            if (change.row in 0 until h && change.col in 0 until w) {
                val composited = pixelCanvasUseCase.getCompositedPixelAt(change.row, change.col)
                bmp[change.col, change.row] = composited
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
                pixelCanvasUseCase.addLayer("Layer ${pixelCanvasUseCase.getLayers().size + 1}")
                refreshLayerState()
                refreshActiveLayerState()
                saveState()
            }
            is LayerEvent.DeleteLayer -> {
                pixelCanvasUseCase.removeLayer(event.layerId)
                refreshLayerState()
                refreshActiveLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
                saveState()
            }
            is LayerEvent.SelectLayer -> {
                pixelCanvasUseCase.setActiveLayer(event.layerId)
                refreshActiveLayerState()
            }
            is LayerEvent.ToggleLock -> {
                pixelCanvasUseCase.toggleLock(event.layerId)
                refreshLayerState()
                saveState()
            }
            is LayerEvent.ToggleVisibility -> {
                pixelCanvasUseCase.toggleVisibility(event.layerId)
                refreshLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
                saveState()
            }
            is LayerEvent.MergeLayerDown -> {
                pixelCanvasUseCase.mergeLayerDown(event.layerId)
                refreshLayerState()
                refreshActiveLayerState()
                viewModelScope.launch {
                    refreshBitmapState()
                }
                saveState()
            }
            is LayerEvent.ReorderLayer -> {
                pixelCanvasUseCase.reorderLayer(fromIndex = event.fromIndex, toIndex = event.toIndex)

                refreshLayerState()

                viewModelScope.launch {
                    refreshBitmapState()
                }
                saveState()
            }
        }
    }

    fun onCanvasEvent(event: PixelCanvasEvent) {
        when (event) {
            is PixelCanvasEvent.OnStrokeStart -> onStrokeStart(event.y, event.x)
            is PixelCanvasEvent.OnStrokeMove -> onStrokeMove(event.y, event.x)
            is PixelCanvasEvent.OnStrokeEnd -> onStrokeEnd()
            is PixelCanvasEvent.OnStrokeCancel -> onStrokeCancel()
            is PixelCanvasEvent.OnTapAt -> onTapAt(event.y, event.x)
        }
    }

    fun onToolSelectorEvent(event: ToolSelectorEvent) {
        when (event) {
            is ToolSelectorEvent.Undo -> undo()
            is ToolSelectorEvent.Redo -> redo()
            is ToolSelectorEvent.OpenSaveImageDialog -> openDialog(DrawingDialog.SaveImage)
            is ToolSelectorEvent.OpenSaveISpriteDialog -> openDialog(DrawingDialog.SaveISprite)
            is ToolSelectorEvent.OpenLoadISpriteDialog -> openDialog(DrawingDialog.LoadISprite)
            is ToolSelectorEvent.SelectTool -> selectTool(tool = event.tool)
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

    // --- Stroke lifecycle ---

    private fun onStrokeStart(row: Int, col: Int) {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        saveState()

        val color = activeColor.value
        val scale = _uiState.value.toolSize
        val update = tool.beginStroke(pixelCanvasUseCase, row, col, color, scale)

        if (tool.commitsImmediately) {
            // eraser: pixels already committed by tool, update main bitmap
            applyChangesToMainBitmap(update.changes)
            _drawVersion++
            _canvasState.value = _canvasState.value.copy(drawVersion = _drawVersion)
        } else {
            // pencil: draw to overlay for preview
            clearOverlayBitmap()
            applyChangesToOverlay(update.changes)
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(overlayVersion = _overlayVersion)
        }
    }

    private fun onStrokeMove(row: Int, col: Int) {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        val update = tool.updateStroke(pixelCanvasUseCase, row, col)

        if (tool.commitsImmediately) {
            applyChangesToMainBitmap(update.changes)
            _drawVersion++
            _canvasState.value = _canvasState.value.copy(drawVersion = _drawVersion)
        } else {
            if (update.isFullPreview) clearOverlayBitmap()
            applyChangesToOverlay(update.changes)
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(overlayVersion = _overlayVersion)
        }
    }

    private fun onStrokeEnd() {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        val finalChanges = tool.endStroke()

        if (!tool.commitsImmediately && finalChanges.isNotEmpty()) {
            // batch commit overlay pixels to layer
            pixelCanvasUseCase.batchSetPixels(finalChanges)
            applyChangesToMainBitmap(finalChanges)
            clearOverlayBitmap()
            _drawVersion++
            _overlayVersion++
            _canvasState.value = _canvasState.value.copy(
                drawVersion = _drawVersion,
                overlayVersion = _overlayVersion
            )
        }

        updateHistoryCurrentState()
    }

    private fun onStrokeCancel() {
        val tool = _uiState.value.selectedTool
        if (tool !is StrokeTool) return

        tool.cancelStroke()
        clearOverlayBitmap()
        _overlayVersion++

        // Pop the state we saved when the stroke started
        undo()
    }

    private fun onTapAt(row: Int, col: Int) {
        val tool = _uiState.value.selectedTool
        val color = activeColor.value
        val size = _uiState.value.toolSize

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
        val currentLayers = pixelCanvasUseCase.getLayers().map {
            it.copy(pixels = it.pixels.copyOf())
        }
    
        canvasHistoryManager.saveState(
            PixelCanvasState(
                width = pixelCanvasUseCase.getCanvasWidth(),
                height = pixelCanvasUseCase.getCanvasHeight(),
                layers = currentLayers,
                activeLayerId = pixelCanvasUseCase.getActiveLayerId()
            )
        )
    }

    private fun updateHistoryCurrentState() {
        val activeLayerId = pixelCanvasUseCase.getActiveLayerId()
        val currentLayers = pixelCanvasUseCase.getLayers().map {
            if (it.id == activeLayerId) {
                it.copy(pixels = it.pixels.copyOf())
            } else {
                it
            }
        }
        
        canvasHistoryManager.setCurrentState(
            PixelCanvasState(
                width = pixelCanvasUseCase.getCanvasWidth(),
                height = pixelCanvasUseCase.getCanvasHeight(),
                layers = currentLayers,
                activeLayerId = activeLayerId
            )
        )
    }

    fun undo() {
        canvasHistoryManager.undo()?.let { state ->
            pixelCanvasUseCase.setCanvas(Sprite(width = state.width, height = state.height, layers = state.layers))
            pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
            
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
        canvasHistoryManager.redo()?.let { state ->
            pixelCanvasUseCase.setCanvas(Sprite(width = state.width, height = state.height, layers = state.layers))
            pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
            
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
        pixelCanvasUseCase.rotateCanvas()
        refreshCanvasSizeState()
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
        saveState()
    }

    fun hFlip() {
        pixelCanvasUseCase.hFlipCanvas()
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
        saveState()
    }

    fun vFlip() {
        pixelCanvasUseCase.vFlipCanvas()
        refreshLayerState()
        viewModelScope.launch {
            refreshBitmapState()
        }
        saveState()
    }

    fun resizeCanvas(width: Int, height: Int) {
        pixelCanvasUseCase.resizeCanvas(width, height)
        refreshCanvasSizeState()
        refreshLayerState()
        saveState()
        viewModelScope.launch {
            refreshBitmapState()
        }
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
        updateHistoryCurrentState()

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
