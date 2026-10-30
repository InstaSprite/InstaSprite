package com.instasprite.app.ui.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasprite.app.R
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.ui.drawing.contract.CursorDrawEvent
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.ui.drawing.dialog.ColorWheelDialog
import com.instasprite.app.ui.drawing.dialog.DrawingSettingsDialog
import com.instasprite.app.ui.drawing.dialog.LoadISpriteDialog
import com.instasprite.app.ui.drawing.dialog.ResizeCanvasDialog
import com.instasprite.app.ui.drawing.dialog.SaveISpriteDialog
import com.instasprite.app.ui.drawing.dialog.SaveImageDialog
import kotlinx.coroutines.launch

sealed interface DrawingDialog : Dialog {
    data object SaveSheet : DrawingDialog
    data object SaveGif : DrawingDialog
    data object SaveImage : DrawingDialog
    data object SaveISprite : DrawingDialog
    data object LoadISprite : DrawingDialog
    data object ResizeCanvas : DrawingDialog
    data object ColorWheel : DrawingDialog
    data object DitherColorWheel : DrawingDialog
    data object DrawingSettings : DrawingDialog
}

@Composable
fun DrawingScreenDialogs(
    dialogState: List<DrawingDialog>,
    viewModel: DrawingViewModel,
    onNavigateToPalette: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    var lastSavedUri = viewModel.lastSavedLocation.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        lastSavedUri = viewModel.getLastSavedLocation()
    }

    dialogState.forEach { dialog ->
        when (dialog) {
            DrawingDialog.SaveISprite ->
                SaveISpriteDialog(
                    onDismiss = viewModel::closeTopDialog,
                    folderUri = lastSavedUri,
                    isSaving = uiState.isSaving,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name -> viewModel.saveISprite(uri, name) }
                )

            DrawingDialog.SaveGif, DrawingDialog.SaveSheet -> {
                val format =
                    if (dialog == DrawingDialog.SaveGif) com.instasprite.app.domain.export.AnimationExportFormat.GIF
                    else com.instasprite.app.domain.export.AnimationExportFormat.SPRITE_SHEET
                val canvas = viewModel.canvasState.collectAsStateWithLifecycle().value
                com.instasprite.app.ui.drawing.dialog.SaveAnimationDialog(
                    format = format,
                    sprite = com.instasprite.app.domain.model.Sprite(
                        width = canvas.width, height = canvas.height,
                        layers = canvas.layers, animation = canvas.animation
                    ),
                    onDismiss = viewModel::closeTopDialog,
                    folderUri = lastSavedUri,
                    isSaving = uiState.isSaving,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name, columns, scale ->
                        viewModel.saveAnimation(
                            format,
                            uri,
                            name,
                            columns,
                            scale
                        )
                    }
                )
            }

            DrawingDialog.SaveImage ->
                SaveImageDialog(
                    onDismiss = viewModel::closeTopDialog,
                    folderUri = lastSavedUri,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name, scale -> viewModel.saveImage(uri, name, scale) },
                    isSaving = uiState.isSaving
                )

            DrawingDialog.LoadISprite ->
                LoadISpriteDialog(
                    onDismiss = viewModel::closeTopDialog,

                    onFilePicked = { uri ->
                        viewModel.getSpriteDataFromFile(uri)
                    },

                    onLoad = {
                        scope.launch { viewModel.loadSprite(it) }
                    }
                )

            DrawingDialog.ResizeCanvas ->
                ResizeCanvasDialog(
                    onDismiss = viewModel::closeTopDialog,
                    currentCanvasWidth = viewModel.canvasState.value.width,
                    currentCanvasHeight = viewModel.canvasState.value.height,
                    onResize = viewModel::resizeCanvas
                )

            DrawingDialog.ColorWheel, DrawingDialog.DitherColorWheel -> {
                val isDither = dialog == DrawingDialog.DitherColorWheel
                val foreground = viewModel.activeColor.collectAsStateWithLifecycle().value
                val background = Color(uiState.dither.secondaryColor)
                ColorWheelDialog(
                    initialColor = if (isDither) {
                        if (background.alpha == 0f) foreground.copy(alpha = 1f) else background
                    } else foreground,
                    colorPalette = viewModel.colorPalette.collectAsStateWithLifecycle().value,
                    onDismiss = viewModel::closeTopDialog,
                    onColorSelected = { color ->
                        if (isDither) viewModel.onToolSelectorEvent(
                            ToolSelectorEvent.SetDitherBackground(
                                color
                            )
                        )
                        else viewModel.selectColor(color)
                    },
                    onOpenPaletteScreen = onNavigateToPalette,
                    showChoosePalette = !isDither
                )
            }

            DrawingDialog.DrawingSettings ->
                DrawingSettingsDialog(
                    isCursorMode = uiState.isCursorMode,
                    showCanvasPreview = uiState.showCanvasPreview,
                    onCursorModeChange = {
                        viewModel.onCursorDrawEvent(CursorDrawEvent.ToggleCursorMode(-1f, -1f))
                    },
                    onShowCanvasPreviewChange = {
                        viewModel.toggleCanvasPreview()
                    },
                    onDismiss = viewModel::closeTopDialog
                )
        }
    }

    if (viewModel.tutorialManager.state.collectAsStateWithLifecycle().value.showWelcomeDialog) {
        com.instasprite.app.ui.components.dialog.CustomDialog(
            title = stringResource(R.string.tut_welcome_title),
            onDismiss = { viewModel.tutorialManager.onEvent(com.instasprite.app.ui.tutorial.TutorialEvent.OnDismiss) },
            onConfirm = { viewModel.tutorialManager.onEvent(com.instasprite.app.ui.tutorial.TutorialEvent.OnStartTutorial) },
            confirmButtonText = stringResource(R.string.tut_btn_show),
            dismissButtonText = stringResource(R.string.tut_btn_skip),
            content = {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.tut_welcome_desc),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = com.instasprite.app.ui.theme.AppTheme.colors.onSurface
                )
            }
        )
    }
}
