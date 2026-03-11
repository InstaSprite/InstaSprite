package com.olaz.instasprite.ui.drawing

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeTool
import com.olaz.instasprite.ui.components.composable.DrawerLayout
import com.olaz.instasprite.ui.components.composable.DrawerSide
import com.olaz.instasprite.ui.drawing.component.ColorPalette
import com.olaz.instasprite.ui.drawing.component.LayerDrawer
import com.olaz.instasprite.ui.drawing.component.PixelCanvas
import com.olaz.instasprite.ui.drawing.component.ShapeSelector
import com.olaz.instasprite.ui.drawing.component.ToolSelector
import com.olaz.instasprite.ui.drawing.component.ToolSizeSlider
import com.olaz.instasprite.ui.drawing.contract.CanvasMenuEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteState
import com.olaz.instasprite.ui.drawing.contract.LayerEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.drawing.contract.ToolSelectorEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.DummyData
import com.olaz.instasprite.utils.UiUtils
import com.olaz.instasprite.utils.calculateNewScaleAndOffset

data class DrawingScreenEvent(
    val onColorPaletteEvent: (ColorPaletteEvent) -> Unit,
    val onCanvasMenuEvent: (CanvasMenuEvent) -> Unit,
    val onToolSelectorEvent: (ToolSelectorEvent) -> Unit,
    val onCanvasEvent: (PixelCanvasEvent) -> Unit,
    val onToolSizeChange: (Int) -> Unit,
    val onToggleLayerDrawer: () -> Unit,
    val onLayerEvent: (LayerEvent) -> Unit
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
        UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColorDarker)
        UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColorDarker)
    } else {
        UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColor)
        UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColor)
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
            onToggleLayerDrawer = viewModel::toggleLayerDrawer,
            onLayerEvent = viewModel::onLayerEvent
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
                overlayBitmap = viewModel.overlayBitmap
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
    overlayBitmap: Bitmap?
) {
    val maxScale by remember(canvasState.width, canvasState.height) {
        derivedStateOf {
            val canvasSize = maxOf(canvasState.width, canvasState.height).toFloat()
            canvasSize.div(8f).coerceAtLeast(2f).coerceAtMost(100f)
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val coroutineScope = rememberCoroutineScope()
    var toolSizeValue by remember { mutableIntStateOf(uiState.toolSize) }

    Scaffold(
        topBar = {
            Column {
                ColorPalette(
                    modifier = Modifier
                        .background(CatppuccinUI.BackgroundColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    colorPaletteState = colorPaletteState,
                    onColorPaletteEvent = event.onColorPaletteEvent,
                    onCanvasMenuEvent = event.onCanvasMenuEvent
                )


            }
        },
        bottomBar = {
            Column {
                ShapeSelector(
                    isVisible = (uiState.selectedTool is ShapeTool),
                    selectedTool = uiState.selectedTool,
                    onShapeSelected = { tool ->
                        event.onToolSelectorEvent(ToolSelectorEvent.SelectTool(tool))
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(CatppuccinUI.BackgroundColor)
                        .padding(12.dp)
                ) {

                    Box(modifier = Modifier.weight(9f)) {
                        if (uiState.selectedTool is StrokeTool) {
                            ToolSizeSlider(
                                toolSizeValue = toolSizeValue,
                                onValueChange = {
                                    toolSizeValue = it
                                    event.onToolSizeChange(it)
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { event.onToggleLayerDrawer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = CatppuccinUI.TextColorLight
                        )
                    }
                }

                ToolSelector(
                    modifier = Modifier
                        .height(66.dp)
                        .background(CatppuccinUI.BackgroundColor)
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    selectedTool = uiState.selectedTool,
                    onToolSelectorEvent = event.onToolSelectorEvent,
                )
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CatppuccinUI.BackgroundColorDarker),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CatppuccinUI.SelectedColor)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CatppuccinUI.BackgroundColorDarker)

            ) {
                PixelCanvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(10.dp)
                        .fillMaxSize()
                        .fillMaxHeight(0.7f),
                    pixelCanvasState = canvasState,
                    bitmap = bitmap,
                    overlayBitmap = overlayBitmap,
                    selectedTool = uiState.selectedTool,
                    scale = scale,
                    offset = offset,
                    onTransform = { centroid, panChange, zoomChange, canvasSize ->
                        val (newScale, newOffset) = calculateNewScaleAndOffset(
                            centroid = centroid,
                            panChange = panChange,
                            zoomChange = zoomChange,
                            currentScale = scale,
                            currentOffset = offset,
                            layoutSize = canvasSize,
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
                onLayerEvent = {},
                onToggleLayerDrawer = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null
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
                onLayerEvent = {},
                onToggleLayerDrawer = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null
        )
    }
}