package com.instasprite.app.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.export.AnimationExportFormat
import com.instasprite.app.ui.gallery.contract.GalleryExportFormat
import com.instasprite.app.ui.drawing.dialog.SaveAnimationDialog
import com.instasprite.app.ui.drawing.dialog.SaveISpriteDialog
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasprite.app.R
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
import com.instasprite.app.ui.drawing.dialog.LoadISpriteDialog
import com.instasprite.app.ui.gallery.dialog.DisplayOptionsDialog
import com.instasprite.app.ui.gallery.dialog.RenameDialog
import com.instasprite.app.ui.drawing.dialog.SaveImageDialog
import com.instasprite.app.ui.theme.AppTheme


sealed interface GalleryDialog : Dialog {
    data class ExportFile(val sprite: Sprite, val name: String, val format: GalleryExportFormat) : GalleryDialog
    data class Rename(val spriteId: String) : GalleryDialog
    data class DeleteSpriteConfirm(val spriteName: String, val spriteId: String) : GalleryDialog
    data object DisplayOptions : GalleryDialog
    data object LoadISprite : GalleryDialog
}

@Composable
fun GalleryScreenDialogs(
    dialogState: List<GalleryDialog>,
    viewModel: GalleryViewModel
) {
    var lastSavedUri = viewModel.lastSavedLocation.collectAsStateWithLifecycle().value
    val spriteListOrder = viewModel.spriteListOrder.collectAsStateWithLifecycle().value
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val layoutMode = uiState.layoutMode

    LaunchedEffect(Unit) {
        lastSavedUri = viewModel.getLastSavedLocation()
    }

    dialogState.forEach { dialog ->
        when (dialog) {
            is GalleryDialog.DeleteSpriteConfirm ->
                ConfirmationDialog(
                    title = "Delete sprite",
                    text = stringResource(R.string.are_you_sure_you_want_to_delete),
                    highlightText = dialog.spriteName,
                    confirmButtonText = stringResource(R.string.delete),
                    dismissButtonText = stringResource(R.string.cancel),
                    highlightTextColor = AppTheme.colors.red,
                    hasQuestionMark = true,
                    onConfirm = {
                        viewModel.deleteSpriteById(dialog.spriteId)
                        viewModel.closeTopDialog()
                    },
                    onDismiss = viewModel::closeTopDialog
                )

            is GalleryDialog.Rename ->
                RenameDialog(
                    spriteId = dialog.spriteId,
                    onSpriteRename = viewModel::renameSprite,
                    onDismiss = viewModel::closeTopDialog
                )

            is GalleryDialog.ExportFile -> {
                val name = dialog.name.ifBlank { "Sprite" }
                when (dialog.format) {
                    GalleryExportFormat.IMAGE -> SaveImageDialog(
                        onDismiss = viewModel::closeTopDialog,
                        folderUri = lastSavedUri,
                        onFolderSelected = viewModel::setLastSavedLocation,
                        isSaving = uiState.isExporting,
                        initialName = name,
                        onSave = { uri, fileName, scale ->
                            viewModel.exportSprite(dialog.sprite, dialog.format, uri, fileName, scale)
                        }
                    )
                    GalleryExportFormat.PROJECT -> SaveISpriteDialog(
                        onDismiss = viewModel::closeTopDialog,
                        folderUri = lastSavedUri,
                        onFolderSelected = viewModel::setLastSavedLocation,
                        isSaving = uiState.isExporting,
                        initialName = name,
                        onSave = { uri, fileName ->
                            viewModel.exportSprite(dialog.sprite, dialog.format, uri, fileName)
                        }
                    )
                    GalleryExportFormat.GIF, GalleryExportFormat.SPRITE_SHEET -> SaveAnimationDialog(
                        format = if (dialog.format == GalleryExportFormat.GIF) AnimationExportFormat.GIF else AnimationExportFormat.SPRITE_SHEET,
                        sprite = dialog.sprite,
                        onDismiss = viewModel::closeTopDialog,
                        folderUri = lastSavedUri,
                        isSaving = uiState.isExporting,
                        initialName = name,
                        onFolderSelected = viewModel::setLastSavedLocation,
                        onSave = { uri, fileName, columns, scale ->
                            viewModel.exportSprite(dialog.sprite, dialog.format, uri, fileName, scale, columns)
                        }
                    )
                }
            }

            is GalleryDialog.DisplayOptions ->
                DisplayOptionsDialog(
                    onSortOrderSelected = { sortOrder ->
                        viewModel.setSpriteListOrder(order = sortOrder)
                    },
                    spriteListOrder = spriteListOrder,
                    onLayoutModeSelected = viewModel::setLayoutMode,
                    layoutMode = layoutMode,
                    onDismiss = viewModel::closeTopDialog
                )

            is GalleryDialog.LoadISprite ->
                LoadISpriteDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onFilePicked = { uri -> viewModel.getSpriteDataFromFile(uri) },
                    onLoad = { sprite -> viewModel.importSprite(sprite) }
                )
        }
    }
}
