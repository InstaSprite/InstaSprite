package com.olaz.instasprite.ui.drawing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.data.repository.PixelCanvasRepository
import com.olaz.instasprite.data.repository.SpriteDatabaseRepository
import com.olaz.instasprite.data.repository.StorageLocationRepository
import com.olaz.instasprite.domain.canvashistory.CanvasHistoryManager
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.usecase.LoadFileUseCase
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase
import com.olaz.instasprite.domain.usecase.SaveFileUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class DrawingScreenState(
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
    private val saveFileUseCase = SaveFileUseCase()
    private val loadFileUseCase = LoadFileUseCase()
    private val pixelCanvasUseCase = PixelCanvasUseCase(
        pixelCanvasRepository = pixelCanvasRepository,
        colorPaletteRepository = colorPaletteRepository
    )

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
            pixels = pixelCanvasUseCase.getAllPixels(),
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
            loadFromDB()
            saveToDB(spriteName)
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
                saveState()
                pixelCanvasUseCase.addLayer("Layer ${pixelCanvasUseCase.getLayers().size + 1}")
                refreshCanvasState()
            }
            is LayerEvent.DeleteLayer -> {
                saveState()
                pixelCanvasUseCase.removeLayer(event.layerId)
                refreshCanvasState()
            }
            is LayerEvent.SelectLayer -> {
                pixelCanvasUseCase.setActiveLayer(event.layerId)
                refreshCanvasState()
            }
            is LayerEvent.ToggleLock -> {
                saveState()
                pixelCanvasUseCase.toggleLock(event.layerId)
                refreshCanvasState()
            }
            is LayerEvent.ToggleVisibility -> {
                saveState()
                pixelCanvasUseCase.toggleVisibility(event.layerId)
                refreshCanvasState()
            }
            is LayerEvent.MergeLayerDown -> {
                saveState()
                pixelCanvasUseCase.mergeLayerDown(event.layerId)
                refreshCanvasState()
            }
            is LayerEvent.ReorderLayer -> {
                saveState()
                pixelCanvasUseCase.reorderLayer(fromIndex = event.fromIndex, toIndex = event.toIndex)
                refreshCanvasState()
            }
        }
    }

    fun onCanvasEvent(event: PixelCanvasEvent) {
        when (event) {
            is PixelCanvasEvent.OnCanvasTouchStart -> saveState()
            is PixelCanvasEvent.DrawAt -> applyTool(event.y, event.x)

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

    fun applyTool(
        row: Int,
        col: Int,
    ) {
        val tool = _uiState.value.selectedTool
        val color = activeColor.value
        val size = _uiState.value.toolSize

        Log.d(
            "DrawingScreenViewModel",
            "Applying tool: ${tool.name} at row=$row, col=$col with color=${color.value}"
        )

        tool.apply(pixelCanvasUseCase, row, col, color, size)
        refreshCanvasState()
    }

    fun getPixelData(row: Int, col: Int): Color {
        return pixelCanvasUseCase.getPixel(row, col)
    }

    fun saveState() {
        val currentLayers = pixelCanvasUseCase.getLayers().map {
            it.copy(pixels = it.pixels.toList()) // deep copy pixels for history immutability
        }
    
        canvasHistoryManager.saveState(
            PixelCanvasState(
                width = pixelCanvasUseCase.getCanvasWidth(),
                height = pixelCanvasUseCase.getCanvasHeight(),
                pixels = pixelCanvasUseCase.getAllPixels(),
                layers = currentLayers,
                activeLayerId = pixelCanvasUseCase.getActiveLayerId()
            )
        )
    }

    fun undo() {
        canvasHistoryManager.undo()?.let { state ->
            pixelCanvasUseCase.setCanvas(Sprite(width = state.width, height = state.height, layers = state.layers))
            pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
            
            _canvasState.value = _canvasState.value.copy(
                width = state.width,
                height = state.height,
            )

            refreshCanvasState()
        }
    }

    fun redo() {
        canvasHistoryManager.redo()?.let { state ->
            pixelCanvasUseCase.setCanvas(Sprite(width = state.width, height = state.height, layers = state.layers))
            pixelCanvasUseCase.setActiveLayer(state.activeLayerId)
            
            _canvasState.value = _canvasState.value.copy(
                width = state.width,
                height = state.height,
            )

            refreshCanvasState()
        }
    }

    fun rotate() {
        pixelCanvasUseCase.rotateCanvas()
        refreshCanvasState()
        saveState()
    }

    fun hFlip() {
        pixelCanvasUseCase.hFlipCanvas()
        refreshCanvasState()
        saveState()
    }

    fun vFlip() {
        pixelCanvasUseCase.vFlipCanvas()
        refreshCanvasState()
        saveState()
    }

    fun resizeCanvas(width: Int, height: Int) {
        pixelCanvasUseCase.resizeCanvas(width, height)
        _canvasState.value = _canvasState.value.copy(
            width = width,
            height = height
        )
        saveState()
        refreshCanvasState()
    }

    private fun refreshCanvasState() {
        val newPixels = pixelCanvasUseCase.getAllPixels()
        val newWidth = pixelCanvasUseCase.getCanvasWidth()
        val newHeight = pixelCanvasUseCase.getCanvasHeight()

        _canvasState.value = _canvasState.value.copy(
            pixels = newPixels,
            width = newWidth,
            height = newHeight,
            layers = pixelCanvasUseCase.getLayers().toList(),
            activeLayerId = pixelCanvasUseCase.getActiveLayerId()
        )
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
        context: Context,
        folderUri: Uri,
        fileName: String,
        scalePercent: Int = 100
    ): Boolean {
        val result = saveFileUseCase.saveImageFile(
            context,
            pixelCanvasUseCase.getSprite(),
            scalePercent,
            folderUri,
            fileName
        )

        result.fold(
            onSuccess = { return true },
            onFailure = { exception ->
                Log.e("SaveFile", "Failed to save file", exception)
                return false
            }
        )
    }

    fun saveISprite(
        context: Context,
        folderUri: Uri,
        fileName: String
    ): Boolean {
        val result = saveFileUseCase.saveISpriteFile(
            context,
            pixelCanvasUseCase.getSprite(),
            folderUri,
            fileName
        )

        result.fold(
            onSuccess = { return true },
            onFailure = { exception ->
                Log.e("SaveFile", "Failed to save file", exception)
                return false
            }
        )
    }

    fun getSpriteDataFromFile(context: Context, fileUri: Uri): Sprite? {
        return loadFileUseCase.loadFile(context, fileUri)
    }

    fun loadSprite(sprite: Sprite) {
        setCanvasSize(sprite.width, sprite.height)
        pixelCanvasUseCase.setCanvas(sprite)
        sprite.colorPalette?.let {
            colorPaletteRepository.updatePalette(sprite.colorPalette.map { Color(it) })
        }
        canvasHistoryManager.reset()
        saveState()
        refreshCanvasState()
    }

    suspend fun saveToDB(spriteName: String? = null) {
        val sprite = pixelCanvasUseCase.getSprite()
        spriteDataRepository.saveSprite(sprite.copy(id = spriteId))
        spriteName?.let {
            spriteDataRepository.changeName(spriteId, it)
        }
    }

    suspend fun loadFromDB() {
        val sprite = spriteDataRepository.loadSprite(spriteId)
        if (sprite != null) {
            loadSprite(sprite)
        }
    }

    suspend fun importColorsFromLospecUrl(url: String): List<Color> {
        return colorPaletteRepository.getLospecColorPalette(url)?.colors ?: emptyList()

    }

    fun updateColorPalette(colors: List<Color>) {
        if (colors.isNotEmpty()) {
            colorPaletteRepository.updatePalette(colors)
        }
    }
}