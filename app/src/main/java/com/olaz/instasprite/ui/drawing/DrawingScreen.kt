package com.olaz.instasprite.ui.drawing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.domain.tool.EraserTool
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.ui.drawing.component.ColorPalette
import com.olaz.instasprite.ui.drawing.component.PixelCanvas
import com.olaz.instasprite.ui.drawing.component.ToolSelector
import com.olaz.instasprite.ui.drawing.component.ToolSizeSlider
import com.olaz.instasprite.ui.drawing.contract.CanvasMenuEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteState
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.drawing.contract.ToolSelectorEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.DummyData
import com.olaz.instasprite.utils.UiUtils
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.zoomable

data class DrawingScreenEvent(
    val onColorPaletteEvent: (ColorPaletteEvent) -> Unit,
    val onCanvasMenuEvent: (CanvasMenuEvent) -> Unit,
    val onToolSelectorEvent: (ToolSelectorEvent) -> Unit,
    val onCanvasEvent: (PixelCanvasEvent) -> Unit,
    val onToolSizeChange: (Int) -> Unit
)

@Composable
fun DrawingScreen(
    onNavigateBack: (String) -> Unit,
    onNavigateToPalette: () -> Unit,
    viewModel: DrawingViewModel = hiltViewModel()
) {
    BackHandler(onBack = { onNavigateBack(viewModel.spriteId) })

    UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColor)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColor)

    val colorPaletteState by viewModel.colorPaletteState.collectAsState()
    val canvasState by viewModel.canvasState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    DrawingScreenDialogs(dialogState, viewModel)

    val event = remember(viewModel) {
        viewModel.onOpenPalette = onNavigateToPalette

        DrawingScreenEvent(
            onColorPaletteEvent = viewModel::onColorPaletteEvent,
            onCanvasMenuEvent = viewModel::onCanvasMenuEvent,
            onToolSelectorEvent = viewModel::onToolSelectorEvent,
            onCanvasEvent = viewModel::onCanvasEvent,
            onToolSizeChange = viewModel::setToolSize
        )
    }

    DrawingScreenContent(
        uiState = uiState,
        canvasState = canvasState,
        colorPaletteState = colorPaletteState,
        event = event
    )
}

@Composable
private fun DrawingScreenContent(
    uiState: DrawingScreenState,
    canvasState: PixelCanvasState,
    colorPaletteState: ColorPaletteState,
    event: DrawingScreenEvent
) {
    val maxScale by remember(canvasState.width, canvasState.height) {
        derivedStateOf {
            val canvasSize = maxOf(canvasState.width, canvasState.height).toFloat()
            canvasSize.div(8f).coerceAtLeast(2f).coerceAtMost(100f)
        }
    }

    val canvasZoomState = remember(maxScale) {
        ZoomState(maxScale = maxScale)
    }

    val layoutSize = remember { mutableStateOf(IntSize.Zero) }

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

                if (uiState.selectedTool in listOf(PencilTool, EraserTool)) {
                    ToolSizeSlider(
                        toolSizeValue = toolSizeValue,
                        onValueChange = {
                            toolSizeValue = it
                            event.onToolSizeChange(it)
                        }
                    )
                }
            }
        },
        bottomBar = {
            Column {
                Slider(
                    value = canvasZoomState.scale,
                    onValueChange = {
                        coroutineScope.launch {
                            canvasZoomState.changeScale(
                                targetScale = it,
                                position = Offset(
                                    x = layoutSize.value.width / 2f,
                                    y = layoutSize.value.height / 2f
                                )
                            )
                        }
                    },
                    valueRange = 1f..maxScale,
                    colors = SliderDefaults.colors(
                        thumbColor = CatppuccinUI.SelectedColor,
                        activeTrackColor = CatppuccinUI.Foreground0Color,
                        inactiveTrackColor = CatppuccinUI.Foreground0Color
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(CatppuccinUI.BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CatppuccinUI.BackgroundColorDarker)
                .zoomable(
                    zoomState = canvasZoomState,
                    enableOneFingerZoom = false,
                    onTap = null,
                    onDoubleTap = null,
                    onLongPress = null
                )
                .onSizeChanged {
                    layoutSize.value = it
                }
        ) {
            PixelCanvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(10.dp)
                    .fillMaxSize()
                    .fillMaxHeight(0.7f),
                pixelCanvasState = canvasState,
                selectedTool = uiState.selectedTool,
                onEvent = event.onCanvasEvent
            )
        }
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
                height = 16,
                pixels = List(16 * 16) { Color.Transparent }
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
                onToolSizeChange = {}
            )
        )
    }
}