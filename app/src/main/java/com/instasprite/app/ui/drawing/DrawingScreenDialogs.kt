package com.instasprite.app.ui.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.ui.drawing.dialog.ColorWheelDialog
import com.instasprite.app.ui.drawing.dialog.LoadISpriteDialog
import com.instasprite.app.ui.drawing.dialog.ResizeCanvasDialog
import com.instasprite.app.ui.drawing.dialog.SaveISpriteDialog
import com.instasprite.app.ui.drawing.dialog.SaveImageDialog
import kotlinx.coroutines.launch

sealed interface DrawingDialog : Dialog {
    data object SaveImage : DrawingDialog
    data object SaveISprite : DrawingDialog
    data object LoadISprite : DrawingDialog
    data object ResizeCanvas : DrawingDialog
    data object ColorWheel : DrawingDialog
}

@Composable
fun DrawingScreenDialogs(
    dialogState: List<DrawingDialog>,
    viewModel: DrawingViewModel
) {
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsState().value

    var lastSavedUri = viewModel.lastSavedLocation.collectAsState().value

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

            DrawingDialog.SaveImage ->
                SaveImageDialog(
                    onDismiss = viewModel::closeTopDialog,
                    folderUri = lastSavedUri,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name, scale -> viewModel.saveImage(uri, name, scale) }
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

            DrawingDialog.ColorWheel ->
                ColorWheelDialog(
                    initialColor = viewModel.activeColor.collectAsState().value,
                    colorPalette = viewModel.colorPalette.collectAsState().value,
                    onDismiss = viewModel::closeTopDialog,
                    onColorSelected = viewModel::selectColor,
                    onOpenPaletteScreen = {
                        viewModel.onOpenPalette()
                    }
                )
        }
    }
}
