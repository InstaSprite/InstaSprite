package com.instasprite.app.ui.drawing

import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.domain.tool.DitherBrush
import com.instasprite.app.domain.tool.DrawingSymmetry
import android.widget.Toast
import com.instasprite.app.R
import com.instasprite.app.utils.CanvasMemoryException
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.model.DrawSetting
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.data.repository.StorageLocationRepository
import com.instasprite.app.di.DrawingSessionFactory
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.draw.AnimationPlayback
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.EyedropperTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.ToolType
import com.instasprite.app.ui.drawing.contract.CanvasMenuEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteState
import com.instasprite.app.ui.drawing.contract.CursorDrawEvent
import com.instasprite.app.ui.drawing.contract.CursorState
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.ui.drawing.contract.toUiState
import com.instasprite.app.ui.drawing.contract.TimelineEvent
import com.instasprite.app.ui.drawing.tutorial.drawingTutorialSequence
import com.instasprite.app.ui.tutorial.TutorialManager
import com.instasprite.app.utils.AppSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DrawingScreenState(
    val isPlaying: Boolean = false,
    val onionSkinEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedTool: Tool,
    val toolSize: Int,
    val brushShape: BrushShape = BrushShape.Circle,
    val showLayerDrawer: Boolean = false,
    val isAppendSelectionMode: Boolean = false,
    val isCursorMode: Boolean = false,
    val showCanvasPreview: Boolean = true,
    val isPixelPerfect: Boolean = false,
    val symmetry: DrawingSymmetry = DrawingSymmetry(),
    val dither: DitherBrush = DitherBrush(),
    val cursorState: CursorState = CursorState()
)

@HiltViewModel(assistedFactory = DrawingViewModel.Factory::class)
class DrawingViewModel @AssistedInject constructor(
    @Assisted("spriteId") val spriteId: String,
    @Assisted("spriteWidth") private val width: Int,
    @Assisted("spriteHeight") private val height: Int,
    @Assisted("spriteName") private val spriteName: String?,
    private val storageLocationRepository: StorageLocationRepository,
    drawingSessionFactory: DrawingSessionFactory,
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
        if (t is CanvasMemoryException) {
            drawingScope.launch(Dispatchers.Main) {
                Toast.makeText(applicationContext, R.string.operation_memory_limit, Toast.LENGTH_LONG).show()
            }
            return
        }
        if (_fatalError.value != null) return

        try {
            drawingEngine.resetHistory()
        } catch (e: Throwable) {
            // ignore
        }
        try {
            drawingSession.clearHistory()
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

    private val drawingSession = drawingSessionFactory.create(spriteId, drawingScope)
    private val pixelCanvasUseCase = drawingSession.canvas
    private val drawingEngine = drawingSession.engine
    private val initialCanvasWidth = if (width > 0) width else pixelCanvasUseCase.getCanvasWidth()
    private val initialCanvasHeight = if (height > 0) height else pixelCanvasUseCase.getCanvasHeight()

    val bitmap get() = drawingEngine.bitmapManager.bitmap
    val overlayBitmap get() = drawingEngine.bitmapManager.overlayBitmap
    val selectionBitmap get() = drawingEngine.bitmapManager.selectionBitmap
    val canvasState = drawingEngine.canvasState.map { it.toUiState() }.stateIn(
        scope = drawingScope,
        started = SharingStarted.Eagerly,
        initialValue = drawingEngine.canvasState.value.toUiState()
    )

    private val _uiState = MutableStateFlow(
        DrawingScreenState(
            selectedTool = drawingEngine.tools.pencil,
            toolSize = 1,
        )
    )
    val uiState: StateFlow<DrawingScreenState> = _uiState.asStateFlow()

    private val _lastSavedLocation = MutableStateFlow<Uri?>(null)
    val lastSavedLocation: StateFlow<Uri?> = _lastSavedLocation.asStateFlow()

    val colorPalette = colorPaletteRepository.colors
    val activeColor = colorPaletteRepository.activeColor
    val recentColors = colorPaletteRepository.recentColors

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
                    loadFromDB()
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
        stopPlayback()
        super.onCleared()
        drawingSession.close()
    }

    private fun applyDrawSetting(drawSetting: DrawSetting) {
        _uiState.value = _uiState.value.copy(
            showCanvasPreview = drawSetting.showCanvasPreview,
            isPixelPerfect = drawSetting.isPixelPerfect
        )
        if (drawSetting.isCursorMode) {
            toggleCursorMode(-1f, -1f)
        }
    }

    val tutorialManager = TutorialManager(
        sequence = drawingTutorialSequence,
        hasSeenTutorial = AppSettings.getHasSeenAppTutorial(applicationContext),
        onCompleteTutorial = {
            AppSettings.setHasSeenAppTutorial(applicationContext, true)
        }
    )
    val tutorialState = tutorialManager.state

    private var playbackJob: Job? = null
    private var playbackStartFrame: String? = null
    private var timelineJob: Job? = null

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        if (_uiState.value.isPlaying) {
            _uiState.value = _uiState.value.copy(isPlaying = false)
            playbackStartFrame?.let { id ->
                pixelCanvasUseCase.selectFrame(id)
                timelineJob = drawingScope.launch { drawingEngine.refreshFullCanvasState() }
            }
        }
        playbackStartFrame = null
    }

    fun onTimelineEvent(event: TimelineEvent) {
        if (event == TimelineEvent.ToggleOnionSkin) {
            _uiState.value = _uiState.value.copy(onionSkinEnabled = !_uiState.value.onionSkinEnabled)
            return
        }
        if (_uiState.value.isLoading || drawingEngine.isBusy) return
        timelineJob?.cancel()
        val wasPlaying = _uiState.value.isPlaying
        stopPlayback()
        commitPendingTool()
        if (event == TimelineEvent.TogglePlayback) {
            if (wasPlaying || pixelCanvasUseCase.animation.frames.size < 2) return
            val startFrame = pixelCanvasUseCase.activeFrameId
            val playback = AnimationPlayback(pixelCanvasUseCase.animation.frames, startFrame)
            playbackStartFrame = startFrame
            _uiState.value = _uiState.value.copy(isPlaying = true)
            playbackJob = drawingScope.launch {
                val start = SystemClock.elapsedRealtime()
                while (true) {
                    val frame = playback.frameAt(SystemClock.elapsedRealtime() - start)
                    if (frame != pixelCanvasUseCase.activeFrameId) drawingEngine.selectFrame(frame)
                    delay(16)
                }
            }
            return
        }
        timelineJob = drawingScope.launch {
            if (event is TimelineEvent.Select) {
                drawingEngine.selectFrame(event.frameId, event.layerId)
            } else {
                val manager = drawingEngine.animationManager
                when (event) {
                    is TimelineEvent.Insert -> manager.insertFrame(event.empty, event.linked)
                    TimelineEvent.Append -> manager.appendFrame()
                    is TimelineEvent.Duration -> manager.setDuration(event.milliseconds)
                    TimelineEvent.Delete -> manager.deleteFrame()
                    TimelineEvent.ClearCel -> manager.clearCel()
                    TimelineEvent.CopyPrevious -> manager.copyPreviousCel(false)
                    TimelineEvent.LinkPrevious -> manager.copyPreviousCel(true)
                    TimelineEvent.Unlink -> manager.unlinkCel()
                    TimelineEvent.SetPoster -> manager.setPosterFrame()
                    is TimelineEvent.Select, TimelineEvent.TogglePlayback, TimelineEvent.ToggleOnionSkin -> Unit
                }
            }
        }
    }

    fun onCanvasMenuEvent(event: CanvasMenuEvent) {
        if (drawingEngine.isBusy) return
        stopPlayback()
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
        if (drawingEngine.isBusy) return
        stopPlayback()
        try {
            commitPendingTool()
            when (event) {
                is LayerEvent.AddLayer -> drawingEngine.addLayer("Layer ${drawingEngine.canvasState.value.layers.size + 1}")
                is LayerEvent.DeleteLayer -> drawingEngine.removeLayer(event.layerId)
                is LayerEvent.SelectLayer -> drawingEngine.selectLayer(event.layerId)
                is LayerEvent.ToggleClipping -> drawingEngine.toggleClipping(event.layerId)
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
        if (drawingEngine.isBusy) return
        stopPlayback()
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
        if (_uiState.value.isPlaying) {
            stopPlayback()
            return
        }
        if (_uiState.value.isLoading || timelineJob?.isActive == true) return
        if (drawingEngine.isBusy && event !is PixelCanvasEvent.OnStrokeMove &&
            event !is PixelCanvasEvent.OnStrokeEnd && event !is PixelCanvasEvent.OnStrokeCancel) return
        stopPlayback()
        try {
            val tool = _uiState.value.selectedTool
            val color = activeColor.value
            val scale = _uiState.value.toolSize
            val shape = _uiState.value.brushShape

            when (event) {
                is PixelCanvasEvent.OnStrokeStart -> {
                    val strokeTool = tool as? StrokeTool ?: return
                    if (strokeTool is PencilTool) {
                        strokeTool.isPixelPerfect = _uiState.value.isPixelPerfect
                    }
                    drawingEngine.onStrokeStart(
                        strokeTool,
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
        if (drawingEngine.isBusy) return
        stopPlayback()
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

                is ToolSelectorEvent.OpenSaveSheetDialog -> openDialog(DrawingDialog.SaveSheet)
                is ToolSelectorEvent.OpenSaveGifDialog -> openDialog(DrawingDialog.SaveGif)
                is ToolSelectorEvent.OpenSaveImageDialog -> openDialog(DrawingDialog.SaveImage)
                is ToolSelectorEvent.OpenSaveISpriteDialog -> openDialog(DrawingDialog.SaveISprite)
                is ToolSelectorEvent.OpenLoadISpriteDialog -> openDialog(DrawingDialog.LoadISprite)
                is ToolSelectorEvent.OpenDrawingSettingsDialog -> openDialog(DrawingDialog.DrawingSettings)
                ToolSelectorEvent.OpenDitherColorWheel -> openDialog(DrawingDialog.DitherColorWheel)
                is ToolSelectorEvent.SelectTool -> selectTool(type = event.tool)
                is ToolSelectorEvent.ToggleAppendSelectionMode -> {
                    _uiState.value = _uiState.value.copy(
                        isAppendSelectionMode = !_uiState.value.isAppendSelectionMode
                    )
                }
                is ToolSelectorEvent.SetDitherPattern, is ToolSelectorEvent.SetDitherBackground -> {
                    val current = _uiState.value.dither
                    val next = when (event) {
                        is ToolSelectorEvent.SetDitherPattern -> current.copy(pattern = event.pattern)
                        is ToolSelectorEvent.SetDitherBackground -> {
                            colorPaletteRepository.addColorToPalette(event.color)
                            current.copy(secondaryColor = event.color.toArgb())
                        }
                    }
                    drawingEngine.strokeEngine.dither = next
                    _uiState.value = _uiState.value.copy(dither = next)
                }
                is ToolSelectorEvent.SetSymmetryAxisX, is ToolSelectorEvent.SetSymmetryAxisY -> {
                    val current = _uiState.value.symmetry
                    val position = when (event) {
                        is ToolSelectorEvent.SetSymmetryAxisX -> event.position
                        is ToolSelectorEvent.SetSymmetryAxisY -> event.position
                    }
                    if (!position.isFinite()) return
                    val next = if (event is ToolSelectorEvent.SetSymmetryAxisX) {
                        current.copy(axisX = position.coerceIn(0f, pixelCanvasUseCase.getCanvasWidth().toFloat()))
                    } else {
                        current.copy(axisY = position.coerceIn(0f, pixelCanvasUseCase.getCanvasHeight().toFloat()))
                    }
                    drawingEngine.strokeEngine.symmetry = next
                    _uiState.value = _uiState.value.copy(symmetry = next)
                }
                ToolSelectorEvent.ToggleHorizontalSymmetry, ToolSelectorEvent.ToggleVerticalSymmetry -> {
                    val current = _uiState.value.symmetry
                    val next = if (event == ToolSelectorEvent.ToggleHorizontalSymmetry) {
                        current.copy(horizontal = !current.horizontal)
                    } else {
                        current.copy(vertical = !current.vertical)
                    }
                    drawingEngine.strokeEngine.symmetry = next
                    _uiState.value = _uiState.value.copy(symmetry = next)
                }
                is ToolSelectorEvent.TogglePixelPerfect -> {
                    togglePixelPerfect()
                }
            }
        } catch (t: Throwable) {
            handleFatalError(t)
        }
    }

    fun selectTool(type: ToolType) {
        try {
            commitPendingTool()
            val tool = drawingEngine.tools[type]
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
        stopPlayback()
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

    fun toggleCanvasPreview() {
        val newState = !_uiState.value.showCanvasPreview
        _uiState.value = _uiState.value.copy(showCanvasPreview = newState)
        AppSettings.setShowCanvasPreview(applicationContext, newState)
    }

    fun togglePixelPerfect() {
        val newValue = !_uiState.value.isPixelPerfect
        _uiState.value = _uiState.value.copy(isPixelPerfect = newValue)
        AppSettings.setPixelPerfect(applicationContext, newValue)
    }

    fun onCursorDrawEvent(event: CursorDrawEvent) {
        if (_uiState.value.isPlaying) {
            stopPlayback()
            return
        }
        if (timelineJob?.isActive == true) return
        stopPlayback()
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
            val cw = drawingEngine.canvasState.value.width
            val ch = drawingEngine.canvasState.value.height
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
        val maxX = drawingEngine.canvasState.value.width.toFloat() - 0.01f
        val maxY = drawingEngine.canvasState.value.height.toFloat() - 0.01f
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
            if (tool is PencilTool) {
                tool.isPixelPerfect = _uiState.value.isPixelPerfect
            }
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
        if (drawingEngine.isBusy) return false
        try {
            if (fileName.isBlank()) return false
            commitPendingTool()
            val sprite = pixelCanvasUseCase.getSprite()
            val bmp = ImageExporter.convertToBitmap(
                sprite,
                pixelCanvasUseCase.activeFrameId,
                scalePercent
            ) ?: return false

            return try {
                fileRepository.saveFile(bmp, folderUri, fileName)
            } finally {
                bmp.recycle()
            }
        } catch (t: Throwable) {
            handleFatalError(t)
            return false
        }
    }

    suspend fun saveISprite(folderUri: Uri, fileName: String): Boolean {
        drawingEngine.awaitToolIdle()
        try {
            if (fileName.isBlank()) return false
            if (_uiState.value.isSaving) return false

            commitPendingTool()

            _uiState.value = _uiState.value.copy(isSaving = true)
            val sprite = pixelCanvasUseCase.getSprite()
            return try {
                withContext(Dispatchers.IO) {
                    fileRepository.saveISpriteFile(
                        sprite,
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

    suspend fun saveAnimation(format: com.instasprite.app.domain.export.AnimationExportFormat,
        folderUri: Uri, fileName: String, columns: Int, scalePercent: Int): Boolean {
        if (_uiState.value.isSaving || fileName.isBlank()) return false
        _uiState.value = _uiState.value.copy(isSaving = true)
        return try {
            drawingEngine.awaitToolIdle()
            stopPlayback()
            commitPendingTool()
            fileRepository.saveAnimationFile(pixelCanvasUseCase.getSprite(), format, folderUri, fileName, columns, scalePercent)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFatalError(e)
            false
        } finally {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun getSpriteDataFromFile(fileUri: Uri): Sprite? {
        return fileRepository.loadISpriteFile(fileUri)
    }

    suspend fun loadSprite(sprite: Sprite) {
        drawingEngine.awaitToolIdle()
        stopPlayback()
        try {
            drawingEngine.release()
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
        drawingEngine.awaitToolIdle()
        try {
            if (_uiState.value.isSaving) return

            commitPendingTool()

            _uiState.value = _uiState.value.copy(isSaving = true)
            val sprite = pixelCanvasUseCase.getSprite()
            try {
                withContext(Dispatchers.IO) {
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
            val sprite = withContext(Dispatchers.IO) { spriteDataRepository.loadSprite(spriteId) }
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
