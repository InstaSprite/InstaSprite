package com.instasprite.app.ui.drawing

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.model.DrawSetting
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.data.repository.PixelCanvasRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.data.repository.StorageLocationRepository
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.draw.DrawingEngine
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.EyedropperTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.ui.drawing.contract.CanvasMenuEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteState
import com.instasprite.app.ui.drawing.contract.CursorDrawEvent
import com.instasprite.app.ui.drawing.contract.CursorState
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.utils.AppSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DrawingScreenState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedTool: Tool,
    val toolSize: Int,
    val brushShape: BrushShape = BrushShape.Circle,
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
    pixelCanvasRepository: PixelCanvasRepository,
    private val spriteDataRepository: SpriteDatabaseRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
    private val fileRepository: FileRepository,
    dialogController: DialogController<DrawingDialog>,
    @ApplicationContext private val applicationContext: Context
) : ViewModel(), DialogController<DrawingDialog> by dialogController {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("spriteId") spriteId: String,
            @Assisted("spriteWidth") width: Int,
            @Assisted("spriteHeight") height: Int,
            @Assisted("spriteName") spriteName: String?
        ): DrawingViewModel
    }

    private val _fatalError = MutableStateFlow<Throwable?>(null)
    val fatalError: StateFlow<Throwable?> = _fatalError.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleFatalError(throwable)
    }

    private val drawingScope = CoroutineScope(viewModelScope.coroutineContext + exceptionHandler)

    fun handleFatalError(t: Throwable) {
        if (_fatalError.value != null) return

        try {
            drawingEngine.resetHistory()
        } catch (e: Throwable) {
            // ignore
        }
        try {
            drawingEngine.release()
        } catch (e: Throwable) {
            // ignore
        }
        if (t is OutOfMemoryError) {
            System.gc()
        }

        _fatalError.value = t
    }

    private val initialCanvasWidth: Int = if (width > 0) width else pixelCanvasRepository.width
    private val initialCanvasHeight: Int = if (height > 0) height else pixelCanvasRepository.height

    private val pixelCanvasUseCase = PixelCanvasUseCase(
        pixelCanvasRepository = pixelCanvasRepository,
        colorPaletteRepository = colorPaletteRepository
    )

    private val drawingEngine =
        DrawingEngine(pixelCanvasUseCase, colorPaletteRepository, drawingScope)

    val bitmap get() = drawingEngine.bitmapManager.bitmap
    val overlayBitmap get() = drawingEngine.bitmapManager.overlayBitmap
    val selectionBitmap get() = drawingEngine.bitmapManager.selectionBitmap
    val canvasState = drawingEngine.canvasState

    private val _uiState = MutableStateFlow(
        DrawingScreenState(
            selectedTool = PencilTool,
            toolSize = 1,
        )
    )
    val uiState: StateFlow<DrawingScreenState> = _uiState.asStateFlow()

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
        scope = drawingScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ColorPaletteState(
            colorPalette = colorPalette.value,
            activeColor = activeColor.value,
            recentColors = recentColors.value
        )
    )

    init {
        try {
            drawingEngine.setCanvasSize(initialCanvasWidth, initialCanvasHeight)
            applyDrawSetting(AppSettings.getDrawSetting(applicationContext))

            drawingScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        loadFromDB()
                    }
                    drawingEngine.refreshFullCanvasState()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } catch (t: Throwable) {
                    handleFatalError(t)
                }
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    override fun onCleared() {
        super.onCleared()
        drawingEngine.release()
    }

    private fun applyDrawSetting(drawSetting: DrawSetting) {
        if (drawSetting.isCursorMode) {
            toggleCursorMode(-1f, -1f)
        }
    }

    fun onCanvasMenuEvent(event: CanvasMenuEvent) {
        try {
            if (event !is CanvasMenuEvent.OpenResizeDialog) {
                commitPendingTool()
            }
            when (event) {
                is CanvasMenuEvent.RotateCanvas -> drawingEngine.rotate()
                is CanvasMenuEvent.HorizontalFlip -> drawingEngine.hFlip()
                is CanvasMenuEvent.VerticalFlip -> drawingEngine.vFlip()
                is CanvasMenuEvent.OpenResizeDialog -> openDialog(DrawingDialog.ResizeCanvas)
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun onLayerEvent(event: LayerEvent) {
        try {
            commitPendingTool()
            when (event) {
                is LayerEvent.AddLayer -> drawingEngine.addLayer("Layer ${canvasState.value.layers.size + 1}")
                is LayerEvent.DeleteLayer -> drawingEngine.removeLayer(event.layerId)
                is LayerEvent.SelectLayer -> drawingEngine.selectLayer(event.layerId)
                is LayerEvent.ToggleLock -> drawingEngine.toggleLock(event.layerId)
                is LayerEvent.ToggleVisibility -> drawingEngine.toggleVisibility(event.layerId)
                is LayerEvent.MergeLayerDown -> drawingEngine.mergeLayerDown(event.layerId)
                is LayerEvent.ReorderLayer -> drawingEngine.reorderLayer(event.fromIndex, event.toIndex)
                is LayerEvent.SetLayerOpacity -> drawingEngine.setLayerOpacity(event.layerId, event.opacity)
                is LayerEvent.SetBlendMode -> drawingEngine.setLayerBlendMode(event.layerId, event.mode)
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun resizeCanvas(width: Int, height: Int) {
        try {
            drawingEngine.resizeCanvas(width, height)
            val state = _uiState.value.cursorState
            _uiState.value = _uiState.value.copy(
                cursorState = state.copy(
                    cursorX = state.cursorX.coerceIn(0f, width.toFloat()),
                    cursorY = state.cursorY.coerceIn(0f, height.toFloat())
                )
            )
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun selectColor(color: Color) {
        colorPaletteRepository.setActiveColor(color)
    }

    fun onCanvasEvent(event: PixelCanvasEvent) {
        try {
            val tool = _uiState.value.selectedTool
            val color = activeColor.value
            val scale = _uiState.value.toolSize
            val shape = _uiState.value.brushShape

            when (event) {
                is PixelCanvasEvent.OnStrokeStart -> {
                    drawingEngine.onStrokeStart(
                        tool as? StrokeTool ?: return,
                        event.y,
                        event.x,
                        color,
                        scale,
                        shape,
                        event.zoomScale
                    )
                }

                is PixelCanvasEvent.OnStrokeMove -> {
                    drawingEngine.onStrokeMove(tool as? StrokeTool ?: return, event.y, event.x)
                }

                is PixelCanvasEvent.OnStrokeEnd -> {
                    drawingEngine.onStrokeEnd(
                        tool as? StrokeTool ?: return,
                        _uiState.value.isAppendSelectionMode
                    )
                }

                is PixelCanvasEvent.OnStrokeCancel -> {
                    drawingEngine.onStrokeCancel(tool as? StrokeTool ?: return)
                }

                is PixelCanvasEvent.OnTapAt -> {
                    drawingEngine.onTapAt(tool, event.y, event.x, color, scale)
                }

                is PixelCanvasEvent.ClearSelection -> {
                    commitPendingTool()
                    drawingEngine.clearSelection(tool)
                }

                is PixelCanvasEvent.InvertSelection -> {
                    commitPendingTool()
                    drawingEngine.invertSelection()
                }
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun onToolSelectorEvent(event: ToolSelectorEvent) {
        try {
            when (event) {
                is ToolSelectorEvent.Undo -> {
                    val tool = _uiState.value.selectedTool as? StrokeTool
                    if (tool != null && drawingEngine.cancelPendingTool(tool)) {
                        // Cancelled pending state, skip undo
                    } else {
                        drawingEngine.undo()
                    }
                }

                is ToolSelectorEvent.Redo -> {
                    commitPendingTool()
                    drawingEngine.redo()
                }

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
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun selectTool(tool: Tool) {
        try {
            commitPendingTool()
            val state = _uiState.value.cursorState
            
            var previewColor: Color? = null
            if (tool is EyedropperTool) {
                val pixel = pixelCanvasUseCase.getCompositedPixelAt(state.gridY, state.gridX)
                previewColor = if (pixel != 0) Color(pixel) else Color.Transparent
            }
            
            _uiState.value = _uiState.value.copy(
                selectedTool = tool,
                cursorState = state.copy(previewColor = previewColor)
            )
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    private fun commitPendingTool() {
        val prev = _uiState.value.selectedTool
        if (prev is StrokeTool) {
            drawingEngine.commitPendingTool(prev)
        }
    }

    fun setToolSize(size: Int) {
        _uiState.value = _uiState.value.copy(toolSize = size)
    }

    fun setBrushShape(shape: BrushShape) {
        _uiState.value = _uiState.value.copy(brushShape = shape)
    }

    fun toggleLayerDrawer() {
        _uiState.value = _uiState.value.copy(showLayerDrawer = !_uiState.value.showLayerDrawer)
    }

    fun onCursorDrawEvent(event: CursorDrawEvent) {
        try {
            when (event) {
                is CursorDrawEvent.ToggleCursorMode -> toggleCursorMode(event.cursorX, event.cursorY)
                is CursorDrawEvent.MoveCursor -> moveCursor(event.cursorX, event.cursorY)
                is CursorDrawEvent.DrawButtonPressed -> onCursorDrawPressed()
                is CursorDrawEvent.DrawButtonReleased -> onCursorDrawReleased()
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    private fun toggleCursorMode(cx: Float, cy: Float) {
        val current = _uiState.value
        val newCursorMode = !current.isCursorMode
        val cursorState = if (newCursorMode) {
            val cw = canvasState.value.width
            val ch = canvasState.value.height
            val startX = if (cx >= 0f) cx else (cw / 2f)
            val startY = if (cy >= 0f) cy else (ch / 2f)
            
            val clampedX = startX.coerceIn(0f, cw.toFloat() - 0.01f)
            val clampedY = startY.coerceIn(0f, ch.toFloat() - 0.01f)
            
            var previewColor: Color? = null
            if (current.selectedTool is EyedropperTool) {
                val pixel = pixelCanvasUseCase.getCompositedPixelAt(clampedY.toInt(), clampedX.toInt())
                previewColor = if (pixel != 0) Color(pixel) else Color.Transparent
            }
            
            CursorState(
                cursorX = clampedX,
                cursorY = clampedY,
                isVisible = true,
                previewColor = previewColor
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
        val maxX = canvasState.value.width.toFloat() - 0.01f
        val maxY = canvasState.value.height.toFloat() - 0.01f
        val clampedX = cursorX.coerceIn(0f, maxX)
        val clampedY = cursorY.coerceIn(0f, maxY)
        val current = _uiState.value.cursorState

        val prevGridX = current.gridX
        val prevGridY = current.gridY
        val newGridX = clampedX.toInt()
        val newGridY = clampedY.toInt()

        var previewColor: Color? = null
        if (_uiState.value.selectedTool is EyedropperTool) {
            val pixel = pixelCanvasUseCase.getCompositedPixelAt(newGridY, newGridX)
            previewColor = if (pixel != 0) Color(pixel) else Color.Transparent
        }

        _uiState.value = _uiState.value.copy(
            cursorState = current.copy(
                cursorX = clampedX,
                cursorY = clampedY,
                previewColor = previewColor
            )
        )

        if (current.isDrawing && (prevGridX != newGridX || prevGridY != newGridY)) {
            val tool = _uiState.value.selectedTool
            if (tool is StrokeTool) {
                drawingEngine.onStrokeMove(tool, newGridY, newGridX)
            }
        }
    }

    private fun onCursorDrawPressed() {
        val state = _uiState.value.cursorState
        if (!state.isVisible) return
        _uiState.value = _uiState.value.copy(
            cursorState = state.copy(isDrawing = true)
        )

        val tool = _uiState.value.selectedTool
        val color = activeColor.value
        val scale = _uiState.value.toolSize
        val shape = _uiState.value.brushShape

        if (tool is StrokeTool) {
            drawingEngine.onStrokeStart(tool, state.gridY, state.gridX, color, scale, shape, 1f)
        } else {
            drawingEngine.onTapAt(tool, state.gridY, state.gridX, color, scale)
        }
    }

    private fun onCursorDrawReleased() {
        val state = _uiState.value.cursorState
        if (!state.isDrawing) return
        _uiState.value = _uiState.value.copy(
            cursorState = state.copy(isDrawing = false)
        )
        val tool = _uiState.value.selectedTool
        if (tool is StrokeTool) {
            drawingEngine.onStrokeEnd(tool, _uiState.value.isAppendSelectionMode)
        }
    }

    fun onColorPaletteEvent(event: ColorPaletteEvent) {
        when (event) {
            is ColorPaletteEvent.SelectColor -> colorPaletteRepository.setActiveColor(event.color)
            is ColorPaletteEvent.OpenColorWheelDialog -> openDialog(DrawingDialog.ColorWheel)
        }
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

    fun saveImage(folderUri: Uri, fileName: String, scalePercent: Int = 100): Boolean {
        try {
            if (fileName.isBlank()) return false
            val sprite = pixelCanvasUseCase.getSprite()
            val bmp = ImageExporter.convertToBitmap(
                sprite.compositedPixels,
                sprite.width,
                sprite.height,
                scalePercent
            ) ?: return false

            return fileRepository.saveFile(bmp, folderUri, fileName)
        } catch (t: Throwable) {
            handleFatalError(t)
            return false
        }
    }

    suspend fun saveISprite(folderUri: Uri, fileName: String): Boolean {
        try {
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
        } catch (t: Throwable) {
            handleFatalError(t)
            return false
        }
    }

    fun getSpriteDataFromFile(fileUri: Uri): Sprite? {
        return fileRepository.loadISpriteFile(fileUri)
    }

    suspend fun loadSprite(sprite: Sprite) {
        try {
            drawingEngine.setCanvasSize(sprite.width, sprite.height)
            drawingEngine.setCanvas(sprite)
            sprite.colorPalette?.let {
                colorPaletteRepository.updatePalette(sprite.colorPalette.map { Color(it) })
            }
            drawingEngine.resetHistory()
            drawingEngine.refreshFullCanvasState()
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    suspend fun saveToDB(name: String? = null) {
        try {
            if (_uiState.value.isSaving) return

            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                withContext(Dispatchers.IO) {
                    val sprite = pixelCanvasUseCase.getSprite()
                    spriteDataRepository.saveSprite(sprite.copy(id = spriteId))
                    name?.let {
                        spriteDataRepository.changeName(spriteId, it)
                    }
                }
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    suspend fun loadFromDB() {
        try {
            val sprite = spriteDataRepository.loadSprite(spriteId)
            if (sprite != null) {
                loadSprite(sprite)
            } else {
                saveToDB(spriteName)
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun updateColorPalette(colors: List<Color>) {
        if (colors.isNotEmpty()) {
            colorPaletteRepository.updatePalette(colors)
        }
    }
}
