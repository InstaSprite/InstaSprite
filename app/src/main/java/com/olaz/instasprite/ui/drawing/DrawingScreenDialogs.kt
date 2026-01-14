package com.olaz.instasprite.ui.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.olaz.instasprite.domain.dialog.Dialog
import com.olaz.instasprite.ui.drawing.dialog.ColorWheelDialog
import com.olaz.instasprite.ui.drawing.dialog.ImportOptionsDialog
import com.olaz.instasprite.ui.drawing.dialog.LoadISpriteDialog
import com.olaz.instasprite.ui.drawing.dialog.LospecImportDialog
import com.olaz.instasprite.ui.drawing.dialog.ResizeCanvasDialog
import com.olaz.instasprite.ui.drawing.dialog.SaveISpriteDialog
import com.olaz.instasprite.ui.drawing.dialog.SaveImageDialog

sealed interface DrawingDialog : Dialog {
    data object SaveImage : DrawingDialog
    data object SaveISprite : DrawingDialog
    data object LoadISprite : DrawingDialog
    data object ResizeCanvas : DrawingDialog
    data object ColorWheel : DrawingDialog
    data object ImportColorPalettes : DrawingDialog
    data object LospecPaletteImport : DrawingDialog
    data object FilePaletteImport : DrawingDialog
}

@Composable
fun DrawingScreenDialogs(
    dialogState: List<DrawingDialog>,
    viewModel: DrawingViewModel
) {
    val context = LocalContext.current

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
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name -> viewModel.saveISprite(context, uri, name) }
                )

            DrawingDialog.SaveImage ->
                SaveImageDialog(
                    onDismiss = viewModel::closeTopDialog,
                    folderUri = lastSavedUri,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSave = { uri, name, scale -> viewModel.saveImage(context, uri, name, scale) }
                )

            DrawingDialog.LoadISprite ->
                LoadISpriteDialog(
                    onDismiss = viewModel::closeTopDialog,

                    onFilePicked = { uri ->
                        viewModel.getISpriteDataFromFile(context, uri)
                    },

                    onLoad = viewModel::loadISprite
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
                    onOpenImportColorPaletteDialog = {
                        viewModel.openDialog(DrawingDialog.ImportColorPalettes)
                    }
                )

            DrawingDialog.ImportColorPalettes ->
                ImportOptionsDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onLospecSelected = {
                        viewModel.openDialog(DrawingDialog.LospecPaletteImport)
                    },
                    onFileSelected = {
                        viewModel.openDialog(DrawingDialog.FilePaletteImport)
                    }
                )

            DrawingDialog.FilePaletteImport -> {
            }

            DrawingDialog.LospecPaletteImport ->
                LospecImportDialog(
                    onDismiss = {
                        viewModel.closeTopDialog()
                    },
                    onImportColorsFromLospecUrl = viewModel::importColorsFromLospecUrl,
                    onImport = {
                        viewModel.updateColorPalette(it)
                        viewModel.closeTopDialog()
                    }
                )
        }
    }
}