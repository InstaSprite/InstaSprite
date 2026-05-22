package com.instasprite.app.ui.palette

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.runtime.Composable
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
import com.instasprite.app.ui.palette.dialogs.LospecImportDialog
import com.instasprite.app.ui.theme.AppTheme


sealed interface ColorPaletteDialog : Dialog {
    data object LospecPaletteImport : ColorPaletteDialog
    data class DeletePalette(val palette: ColorPalette) : ColorPaletteDialog
}

@Composable
fun ColorPaletteScreenDialogs(
    dialogState: List<ColorPaletteDialog>,
    viewModel: ColorPaletteViewModel
) {

    dialogState.forEach { dialog ->
        when (dialog) {
            ColorPaletteDialog.LospecPaletteImport -> {
                LospecImportDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onImportColorsFromLospecUrl = viewModel::importPaletteFromLospecUrl,
                    onImport = {
                        viewModel.savePalette(it)
                        viewModel.closeTopDialog()
                    }
                )
            }

            is ColorPaletteDialog.DeletePalette -> {
                ConfirmationDialog(
                    title = "Delete palette",
                    text = stringResource(R.string.are_you_sure_you_want_to_delete),
                    highlightText = dialog.palette.name,
                    confirmButtonText = "Delete",
                    dismissButtonText = "Cancel",
                    highlightTextColor = AppTheme.colors.DismissButtonColor,
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