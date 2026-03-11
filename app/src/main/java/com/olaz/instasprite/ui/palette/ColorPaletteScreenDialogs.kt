package com.olaz.instasprite.ui.palette

import androidx.compose.runtime.Composable
import com.olaz.instasprite.domain.dialog.Dialog
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.ui.components.dialog.ConfirmationDialog
import com.olaz.instasprite.ui.palette.dialogs.ImportOptionsDialog
import com.olaz.instasprite.ui.palette.dialogs.LospecImportDialog
import com.olaz.instasprite.ui.theme.CatppuccinUI


sealed interface ColorPaletteDialog : Dialog {
    data object ImportColorPalettes : ColorPaletteDialog
    data object LospecPaletteImport : ColorPaletteDialog
    data object FilePaletteImport : ColorPaletteDialog
    data class DeletePalette(val palette: ColorPalette) : ColorPaletteDialog
}

@Composable
fun ColorPaletteScreenDialogs(
    dialogState: List<ColorPaletteDialog>,
    viewModel: ColorPaletteViewModel
) {

    dialogState.forEach { dialog ->
        when (dialog) {
            ColorPaletteDialog.ImportColorPalettes -> {
                ImportOptionsDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onLospecSelected = {
                        viewModel.openDialog(ColorPaletteDialog.LospecPaletteImport)
                    },
                    onFileSelected = {
                        viewModel.openDialog(ColorPaletteDialog.FilePaletteImport)
                    }
                )
            }

            ColorPaletteDialog.FilePaletteImport -> {

            }

            ColorPaletteDialog.LospecPaletteImport -> {
                LospecImportDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onImportColorsFromLospecUrl = viewModel::importPaletteFromLospecUrl,
                    onImport = {
                        viewModel.savePalette(it)
                        viewModel.closeTopDialog()
                        viewModel.closeTopDialog()
                    }
                )
            }

            is ColorPaletteDialog.DeletePalette -> {
                ConfirmationDialog(
                    title = "Delete palette",
                    text = "Are you sure you want to delete",
                    highlightText = dialog.palette.name,
                    confirmButtonText = "Delete",
                    dismissButtonText = "Cancel",
                    highlightTextColor = CatppuccinUI.DismissButtonColor,
                    hasQuestionMark = true,
                    onConfirm = {
                        viewModel.deletePalette(dialog.palette)
                        viewModel.closeTopDialog()
                    },
                    onDismiss = viewModel::closeTopDialog
                )
            }
        }
    }
}