package com.instasprite.app.ui.drawing

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.ui.components.composable.DrawerLayout
import com.instasprite.app.ui.components.composable.DrawerSide
import com.instasprite.app.ui.drawing.component.BrushShapeSelector
import com.instasprite.app.ui.drawing.component.ColorPalette
import com.instasprite.app.ui.drawing.component.CursorDrawButton
import com.instasprite.app.ui.drawing.component.CursorModeToggle
import com.instasprite.app.ui.drawing.component.LayerDrawer
import com.instasprite.app.ui.drawing.component.PixelCanvas
import com.instasprite.app.ui.drawing.component.SelectionModeSelector
import com.instasprite.app.ui.drawing.component.SelectionToolOption
import com.instasprite.app.ui.drawing.component.ShapeSelector
import com.instasprite.app.ui.drawing.component.ToolSelector
import com.instasprite.app.ui.drawing.component.ToolSizeSlider
import com.instasprite.app.ui.drawing.contract.CanvasMenuEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteEvent
import com.instasprite.app.ui.drawing.contract.ColorPaletteState
import com.instasprite.app.ui.drawing.contract.CursorDrawEvent
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import com.instasprite.app.ui.drawing.contract.PixelCanvasState
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.calculateNewScaleAndOffset

data class DrawingScreenEvent(
    val onColorPaletteEvent: (ColorPaletteEvent) -> Unit,
    val onCanvasMenuEvent: (CanvasMenuEvent) -> Unit,
    val onToolSelectorEvent: (ToolSelectorEvent) -> Unit,
    val onCanvasEvent: (PixelCanvasEvent) -> Unit,
    val onToolSizeChange: (Int) -> Unit,
    val onBrushShapeChange: (BrushShape) -> Unit,
    val onToggleLayerDrawer: () -> Unit,
    val onLayerEvent: (LayerEvent) -> Unit,
    val onCursorDrawEvent: (CursorDrawEvent) -> Unit
)

@Composable
fun DrawingScreen(
    onNavigateBack: (String) -> Unit,
    onNavigateToPalette: () -> Unit,
    viewModel: DrawingViewModel = hiltViewModel()
) {
    BackHandler(onBack = { onNavigateBack(viewModel.spriteId) })


    val colorPaletteState by viewModel.colorPaletteState.collectAsState()
    val canvasState by viewModel.canvasState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    // reverse the layer list in ui since use reverseLayout = true in LazyColumn kinda broke with Reorderable lib
    val uiLayers = canvasState.layers.asReversed()


    if (uiState.showLayerDrawer) {
        UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
        UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)
    } else {
        UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColor)
        UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColor)
    }

    DrawingScreenDialogs(dialogState, viewModel)

    val event = remember(viewModel) {
        viewModel.onOpenPalette = onNavigateToPalette

        DrawingScreenEvent(
            onColorPaletteEvent = viewModel::onColorPaletteEvent,
            onCanvasMenuEvent = viewModel::onCanvasMenuEvent,
            onToolSelectorEvent = viewModel::onToolSelectorEvent,
            onCanvasEvent = viewModel::onCanvasEvent,
            onToolSizeChange = viewModel::setToolSize,
            onBrushShapeChange = viewModel::setBrushShape,
            onToggleLayerDrawer = viewModel::toggleLayerDrawer,
            onLayerEvent = viewModel::onLayerEvent,
            onCursorDrawEvent = viewModel::onCursorDrawEvent
        )
    }

    DrawerLayout(
        isOpen = uiState.showLayerDrawer,
        onDrawerClose = viewModel::toggleLayerDrawer,
        side = DrawerSide.End,
        drawerContent = {
            LayerDrawer(
                layers = uiLayers,
                activeLayerId = canvasState.activeLayerId,
                canvasWidth = canvasState.width,
                canvasHeight = canvasState.height,
                onEvent = event.onLayerEvent,
                onBack = viewModel::toggleLayerDrawer
            )
        },
        content = {
            DrawingScreenContent(
                uiState = uiState,
                canvasState = canvasState,
                colorPaletteState = colorPaletteState,
                event = event,
                bitmap = viewModel.bitmap,
                overlayBitmap = viewModel.overlayBitmap,
                selectionBitmap = viewModel.selectionBitmap
            )
        }
    )
}

@Composable
private fun DrawingScreenContent(
    uiState: DrawingScreenState,
    canvasState: PixelCanvasState,
    colorPaletteState: ColorPaletteState,
    event: DrawingScreenEvent,
    bitmap: Bitmap?,
    overlayBitmap: Bitmap?,
    selectionBitmap: Bitmap?
) {
    val maxScale by remember(canvasState.width, canvasState.height) {
        derivedStateOf {
            val canvasSize = maxOf(canvasState.width, canvasState.height).toFloat()
            canvasSize.div(8f).coerceAtLeast(2f).coerceAtMost(100f)
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasLayoutSize by remember { mutableStateOf(IntSize.Zero) }

    val coroutineScope = rememberCoroutineScope()
    var toolSizeValue by remember { mutableIntStateOf(uiState.toolSize) }
    val canvasBorderColor = AppTheme.colors.BackgroundColor

    Scaffold(
        topBar = {
            Column {
                ColorPalette(
                    modifier = Modifier
                        .background(AppTheme.colors.BackgroundColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    colorPaletteState = colorPaletteState,
                    onColorPaletteEvent = event.onColorPaletteEvent,
                    onCanvasMenuEvent = event.onCanvasMenuEvent
                )


            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.background(AppTheme.colors.BackgroundColor)
            ) {
                SelectionToolOption(
                    isVisible = (canvasState.selectionState != null),
                    isAppendMode = uiState.isAppendSelectionMode,
                    onAppendModeToggle = { event.onToolSelectorEvent(ToolSelectorEvent.ToggleAppendSelectionMode) },
                    onClearSelect = { event.onCanvasEvent(PixelCanvasEvent.ClearSelection) },
                    onInvertSelect = { event.onCanvasEvent(PixelCanvasEvent.InvertSelection) }
                )

                ShapeSelector(
                    isVisible = (uiState.selectedTool is ShapeTool),
                    selectedTool = uiState.selectedTool,
                    onShapeSelected = { tool ->
                        event.onToolSelectorEvent(ToolSelectorEvent.SelectTool(tool))
                    }
                )

                BrushShapeSelector(
                    isVisible = (uiState.selectedTool is StrokeTool && uiState.selectedTool !is SelectionTool && uiState.selectedTool !is ShapeTool),
                    selectedShape = uiState.brushShape,
                    onShapeSelected = event.onBrushShapeChange
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .padding(12.dp)
                ) {

                    Box(modifier = Modifier.weight(8f)) {
                        if (uiState.selectedTool is StrokeTool && uiState.selectedTool !is SelectionTool) {
                            ToolSizeSlider(
                                toolSizeValue = toolSizeValue,
                                onValueChange = {
                                    toolSizeValue = it
                                    event.onToolSizeChange(it)
                                }
                            )
                        } else {
                            SelectionModeSelector(
                                isVisible = (uiState.selectedTool is SelectionTool),
                                selectedTool = uiState.selectedTool,
                                onSelectionToolSelected = { tool ->
                                    event.onToolSelectorEvent(ToolSelectorEvent.SelectTool(tool))
                                }
                            )
                        }
                    }

                    CursorModeToggle(
                        isCursorMode = uiState.isCursorMode,
                        onToggle = {
                            var cx = -1f
                            var cy = -1f
                            if (canvasLayoutSize.width > 0 && canvasState.width > 0) {
                                val cellWidth = canvasLayoutSize.width.toFloat() / canvasState.width
                                val cellHeight =
                                    canvasLayoutSize.height.toFloat() / canvasState.height
                                val centerX = canvasLayoutSize.width / 2f - offset.x / scale
                                val centerY = canvasLayoutSize.height / 2f - offset.y / scale
                                cx = centerX / cellWidth
                                cy = centerY / cellHeight
                            }
                            event.onCursorDrawEvent(CursorDrawEvent.ToggleCursorMode(cx, cy))
                        }
                    )

                    IconButton(
                        onClick = { event.onToggleLayerDrawer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = stringResource(R.string.layers),
                            tint = AppTheme.colors.TextColorLight
                        )
                    }
                }

                ToolSelector(
                    modifier = Modifier
                        .height(66.dp)
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    selectedTool = uiState.selectedTool,
                    onToolSelectorEvent = event.onToolSelectorEvent
                )

                if (uiState.isCursorMode) {
                    CursorDrawButton(
                        selectedTool = uiState.selectedTool,
                        onPressed = { event.onCursorDrawEvent(CursorDrawEvent.DrawButtonPressed) },
                        onReleased = { event.onCursorDrawEvent(CursorDrawEvent.DrawButtonReleased) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(60.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.BackgroundColorDarker),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(AppTheme.colors.BackgroundColorDarker)

            ) {
                PixelCanvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(5.dp)
                        .fillMaxSize()
                        .fillMaxHeight(0.7f)
                        .clipToBounds(),
                    pixelCanvasState = canvasState,
                    bitmap = bitmap,
                    overlayBitmap = overlayBitmap,
                    selectionBitmap = selectionBitmap,
                    selectedTool = uiState.selectedTool,
                    isSelectionAppendMode = uiState.isAppendSelectionMode,
                    scale = scale,
                    offset = offset,
                    isCursorMode = uiState.isCursorMode,
                    cursorState = uiState.cursorState,
                    toolSize = uiState.toolSize,
                    brushShape = uiState.brushShape,
                    activeColor = colorPaletteState.activeColor,
                    onCursorDrawEvent = event.onCursorDrawEvent,
                    onTransform = { centroid, panChange, zoomChange, layoutSize ->
                        canvasLayoutSize = layoutSize
                        val (newScale, newOffset) = calculateNewScaleAndOffset(
                            centroid = centroid,
                            panChange = panChange,
                            zoomChange = zoomChange,
                            currentScale = scale,
                            currentOffset = offset,
                            layoutSize = layoutSize,
                            maxScale = maxScale
                        )
                        scale = newScale
                        offset = newOffset
                    },
                    onEvent = event.onCanvasEvent
                )
            }
        }
    }
}


@Preview
@Composable
private fun DrawingScreenPreviewLoading() {

    val context = LocalContext.current
    val colorPaletteRepository = ColorPaletteRepository(
        context,
        colorPaletteDao = DummyData.MockClass.MockColorPaletteDao(),
        lospecService = DummyData.MockClass.MockLospecService()
    )
    val colors = colorPaletteRepository.colors.collectAsState()
    val activeColor = colorPaletteRepository.activeColor.collectAsState()

    InstaSpriteTheme {
        DrawingScreenContent(
            uiState = DrawingScreenState(
                isLoading = false,
                selectedTool = PencilTool,
                toolSize = 1
            ),
            canvasState = PixelCanvasState(
                width = 16,
                height = 16
            ),
            colorPaletteState = ColorPaletteState(
                colorPalette = colors.value,
                activeColor = activeColor.value,
                recentColors = emptyList()
            ),
            event = DrawingScreenEvent(
                onColorPaletteEvent = {},
                onCanvasMenuEvent = {},
                onToolSelectorEvent = {},
                onCanvasEvent = {},
                onToolSizeChange = {},
                onBrushShapeChange = {},
                onLayerEvent = {},
                onToggleLayerDrawer = {},
                onCursorDrawEvent = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null,
            selectionBitmap = null
        )
    }
}

@Preview
@Composable
private fun DrawingScreenPreview() {

    val context = LocalContext.current
    val colorPaletteRepository = ColorPaletteRepository(
        context,
        colorPaletteDao = DummyData.MockClass.MockColorPaletteDao(),
        lospecService = DummyData.MockClass.MockLospecService()
    )
    val colors = colorPaletteRepository.colors.collectAsState()
    val activeColor = colorPaletteRepository.activeColor.collectAsState()

    InstaSpriteTheme {
        DrawingScreenContent(
            uiState = DrawingScreenState(
                selectedTool = PencilTool,
                toolSize = 1
            ),
            canvasState = PixelCanvasState(
                width = 16,
                height = 16
            ),
            colorPaletteState = ColorPaletteState(
                colorPalette = colors.value,
                activeColor = activeColor.value,
                recentColors = emptyList()
            ),
            event = DrawingScreenEvent(
                onColorPaletteEvent = {},
                onCanvasMenuEvent = {},
                onToolSelectorEvent = {},
                onCanvasEvent = {},
                onToolSizeChange = {},
                onBrushShapeChange = {},
                onLayerEvent = {},
                onToggleLayerDrawer = {},
                onCursorDrawEvent = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null,
            selectionBitmap = null
        )
    }
}