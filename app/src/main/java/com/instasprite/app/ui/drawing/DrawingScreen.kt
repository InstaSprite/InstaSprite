package com.instasprite.app.ui.drawing

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
import com.instasprite.app.ui.components.composable.DrawerLayout
import com.instasprite.app.ui.components.composable.DrawerSide
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.drawing.component.ColorPalette
import com.instasprite.app.ui.drawing.component.CursorDrawButton
import com.instasprite.app.ui.drawing.component.LayerDrawer
import com.instasprite.app.ui.drawing.component.PixelCanvas
import com.instasprite.app.ui.drawing.component.SelectionToolOption
import com.instasprite.app.ui.drawing.component.ToolOptionMenu
import com.instasprite.app.ui.drawing.component.ToolSelector
import com.instasprite.app.ui.drawing.component.toolOptions
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
import com.instasprite.app.ui.tutorial.SpotlightOverlay
import com.instasprite.app.ui.tutorial.TutorialEvent
import com.instasprite.app.ui.tutorial.TutorialState
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.calculateNewScaleAndOffset
import com.instasprite.app.utils.pixelDp

data class DrawingScreenEvent(
    val onColorPaletteEvent: (ColorPaletteEvent) -> Unit,
    val onCanvasMenuEvent: (CanvasMenuEvent) -> Unit,
    val onToolSelectorEvent: (ToolSelectorEvent) -> Unit,
    val onCanvasEvent: (PixelCanvasEvent) -> Unit,
    val onToolSizeChange: (Int) -> Unit,
    val onBrushShapeChange: (BrushShape) -> Unit,
    val onToggleLayerDrawer: () -> Unit,
    val onLayerEvent: (LayerEvent) -> Unit,
    val onCursorDrawEvent: (CursorDrawEvent) -> Unit,
    val onTutorialEvent: (com.instasprite.app.ui.tutorial.TutorialEvent) -> Unit
)

@Composable
fun DrawingScreen(
    onNavigateBack: (String) -> Unit,
    onNavigateBackDirectly: () -> Unit,
    onNavigateToPalette: () -> Unit,
    viewModel: DrawingViewModel = hiltViewModel()
) {
    BackHandler(onBack = { onNavigateBack(viewModel.spriteId) })

    val fatalError by viewModel.fatalError.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(fatalError) {
        fatalError?.let { throwable ->
            val isOom = throwable is OutOfMemoryError
            val message = if (isOom) {
                "Out of memory"
            } else {
                "A critical error occurred: ${throwable.localizedMessage ?: "Unknown error"}. Attempting recovery save..."
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            try {
                viewModel.saveToDB()
                Toast.makeText(context, "Recovery save successful!", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Toast.makeText(
                    context,
                    "Recovery save failed: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            onNavigateBackDirectly()
        }
    }

    val colorPaletteState by viewModel.colorPaletteState.collectAsState()
    val canvasState by viewModel.canvasState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val tutorialState by viewModel.tutorialState.collectAsState()

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
            onCursorDrawEvent = viewModel::onCursorDrawEvent,
            onTutorialEvent = viewModel.tutorialManager::onEvent
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
                tutorialState = tutorialState,
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
    tutorialState: TutorialState,
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

    var undoBounds by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    var redoBounds by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }

    LaunchedEffect(undoBounds, redoBounds) {
        if (undoBounds != androidx.compose.ui.geometry.Rect.Zero && redoBounds != androidx.compose.ui.geometry.Rect.Zero) {
            val merged = androidx.compose.ui.geometry.Rect(
                left = undoBounds.left,
                top = kotlin.math.min(undoBounds.top, redoBounds.top),
                right = redoBounds.right,
                bottom = kotlin.math.max(undoBounds.bottom, redoBounds.bottom)
            )
            event.onTutorialEvent(
                com.instasprite.app.ui.tutorial.TutorialEvent.OnBoundsChanged(
                    com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.UNDO_REDO,
                    merged
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    ColorPalette(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                event.onTutorialEvent(
                                    TutorialEvent.OnBoundsChanged(
                                        com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.COLOR_PALETTE,
                                        coords.boundsInRoot()
                                    )
                                )
                            }
                            .background(AppTheme.colors.BackgroundColor)
                            .padding(horizontal = 6.pixelDp, vertical = 2.pixelDp),
                        activeColorModifier = Modifier.onGloballyPositioned { coords ->
                            event.onTutorialEvent(
                                TutorialEvent.OnBoundsChanged(
                                    com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.COLOR_WHEEL,
                                    coords.boundsInRoot()
                                )
                            )
                        },
                        canvasMenuModifier = Modifier.onGloballyPositioned { coords ->
                            event.onTutorialEvent(
                                TutorialEvent.OnBoundsChanged(
                                    com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.CANVAS_MENU,
                                    coords.boundsInRoot()
                                )
                            )
                        },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .height(44.pixelDp)
                            .padding(horizontal = 4.pixelDp)
                    ) {
                        ToolOptionMenu(
                            selectedTool = uiState.selectedTool,
                            modifier = Modifier
                                .weight(8f)
                                .onGloballyPositioned { coords ->
                                    event.onTutorialEvent(
                                        TutorialEvent.OnBoundsChanged(
                                            com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.TOOL_OPTION,
                                            coords.boundsInRoot()
                                        )
                                    )
                                }
                                .padding(horizontal = 4.pixelDp, vertical = 2.pixelDp)
                        ) {
                            toolOptions(
                                tool = uiState.selectedTool,
                                uiState = uiState,
                                event = event,
                                toolSize = toolSizeValue,
                                onToolSizeChange = {
                                    toolSizeValue = it
                                    event.onToolSizeChange(it)
                                }
                            )
                        }

                        IconButton(
                            onClick = { event.onToggleLayerDrawer() },
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    event.onTutorialEvent(
                                        TutorialEvent.OnBoundsChanged(
                                            com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.LAYER_TOGGLE,
                                            coords.boundsInRoot()
                                        )
                                    )
                                }
                        ) {
                            PixelIcon(
                                icon = R.drawable.ic_layer,
                                contentDescription = stringResource(R.string.layers),
                            )
                        }
                    }

                    ToolSelector(
                        modifier = Modifier
                            .height(44.pixelDp)
                            .padding(horizontal = 4.pixelDp, vertical = 4.pixelDp),
                        activeToolModifier = Modifier.onGloballyPositioned { coords ->
                            event.onTutorialEvent(
                                TutorialEvent.OnBoundsChanged(
                                    com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.TOOL_BUTTON,
                                    coords.boundsInRoot()
                                )
                            )
                        },
                        undoModifier = Modifier.onGloballyPositioned { coords ->
                            undoBounds = coords.boundsInRoot()
                        },
                        redoModifier = Modifier.onGloballyPositioned { coords ->
                            redoBounds = coords.boundsInRoot()
                        },
                        projectMenuModifier = Modifier.onGloballyPositioned { coords ->
                            event.onTutorialEvent(
                                TutorialEvent.OnBoundsChanged(
                                    com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.PROJECT_MENU,
                                    coords.boundsInRoot()
                                )
                            )
                        },
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
                                .height(40.pixelDp)
                                .padding(vertical = 2.pixelDp)
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
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(AppTheme.colors.BackgroundColorDarker)

                ) {
                    if (canvasState.selectionState != null && !uiState.showLayerDrawer) {
                        SelectionToolOption(
                            isAppendMode = uiState.isAppendSelectionMode,
                            onAppendModeToggle = { event.onToolSelectorEvent(ToolSelectorEvent.ToggleAppendSelectionMode) },
                            onClearSelect = { event.onCanvasEvent(PixelCanvasEvent.ClearSelection) },
                            onInvertSelect = { event.onCanvasEvent(PixelCanvasEvent.InvertSelection) },
                            modifier = Modifier
                                .padding(bottom = 16.pixelDp)
                                .align(Alignment.BottomCenter),
                        )
                    }
                    PixelCanvas(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                event.onTutorialEvent(
                                    TutorialEvent.OnBoundsChanged(
                                        com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep.CANVAS,
                                        coords.boundsInRoot()
                                    )
                                )
                            }
                            .padding(4.pixelDp)
                            .fillMaxSize()
                            .fillMaxHeight(0.7f)
                            .clipToBounds(),
                        pixelCanvasState = canvasState,
                        bitmap = bitmap,
                        overlayBitmap = overlayBitmap,
                        selectionBitmap = selectionBitmap,
                        selectedTool = uiState.selectedTool,
                        isSelectionAppendMode = uiState.isAppendSelectionMode,
                        isShowPreview = uiState.showCanvasPreview,
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

        if (tutorialState.showTutorial && tutorialState.currentStep != null) {
            SpotlightOverlay(
                currentStep = tutorialState.currentStep,
                activeRect = tutorialState.bounds[tutorialState.currentStep]
                    ?: androidx.compose.ui.geometry.Rect.Zero,
                isLastStep = tutorialState.currentStep == com.instasprite.app.ui.drawing.tutorial.drawingTutorialSequence.last(),
                onNext = { event.onTutorialEvent(TutorialEvent.OnNextStep) },
                onSkip = { event.onTutorialEvent(TutorialEvent.OnDismiss) }
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
            tutorialState = TutorialState(),
            event = DrawingScreenEvent(
                onColorPaletteEvent = {},
                onCanvasMenuEvent = {},
                onToolSelectorEvent = {},
                onCanvasEvent = {},
                onToolSizeChange = {},
                onBrushShapeChange = {},
                onLayerEvent = {},
                onToggleLayerDrawer = {},
                onCursorDrawEvent = {},
                onTutorialEvent = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null,
            selectionBitmap = null
        )
    }
}

@Preview
@Composable
private fun DrawingScreenPreviewSelection() {

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
                selectedTool = RectangleSelectionTool,
                toolSize = 1
            ),
            canvasState = PixelCanvasState(
                width = 16,
                height = 16,
                selectionState = SelectionState(
                    mask = BooleanArray(16 * 16),
                    bounds = Rect(3, 3, 3, 3),
                    canvasWidth = 16,
                    canvasHeight = 16
                )
            ),
            colorPaletteState = ColorPaletteState(
                colorPalette = colors.value,
                activeColor = activeColor.value,
                recentColors = emptyList()
            ),
            tutorialState = TutorialState(),
            event = DrawingScreenEvent(
                onColorPaletteEvent = {},
                onCanvasMenuEvent = {},
                onToolSelectorEvent = {},
                onCanvasEvent = {},
                onToolSizeChange = {},
                onBrushShapeChange = {},
                onLayerEvent = {},
                onToggleLayerDrawer = {},
                onCursorDrawEvent = {},
                onTutorialEvent = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null,
            selectionBitmap = null
        )
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
            tutorialState = com.instasprite.app.ui.tutorial.TutorialState(),
            event = DrawingScreenEvent(
                onColorPaletteEvent = {},
                onCanvasMenuEvent = {},
                onToolSelectorEvent = {},
                onCanvasEvent = {},
                onToolSizeChange = {},
                onBrushShapeChange = {},
                onLayerEvent = {},
                onToggleLayerDrawer = {},
                onCursorDrawEvent = {},
                onTutorialEvent = {}
            ),
            bitmap = createBitmap(16, 16),
            overlayBitmap = null,
            selectionBitmap = null
        )
    }
}