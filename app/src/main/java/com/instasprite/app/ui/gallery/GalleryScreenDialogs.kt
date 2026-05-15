package com.instasprite.app.ui.gallery

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
import com.instasprite.app.ui.gallery.dialog.RenameDialog
import com.instasprite.app.ui.gallery.dialog.SaveImageDialog
import com.instasprite.app.ui.gallery.dialog.DisplayOptionsDialog
import com.instasprite.app.ui.drawing.dialog.LoadISpriteDialog
import com.instasprite.app.ui.theme.AppTheme


sealed interface GalleryDialog : Dialog {
    data class SaveImage(val sprite: SpriteWithMeta) : GalleryDialog
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
    var lastSavedUri = viewModel.lastSavedLocation.collectAsState().value
    val spriteListOrder = viewModel.spriteListOrder.collectAsState().value
    val layoutMode = viewModel.uiState.collectAsState().value.layoutMode

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
                    confirmButtonText = "Delete",
                    dismissButtonText = "Cancel",
                    highlightTextColor = AppTheme.colors.DismissButtonColor,
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

            is GalleryDialog.SaveImage ->
                SaveImageDialog(
                    spriteName = dialog.sprite.meta!!.spriteName,
                    spriteId = dialog.sprite.sprite.id,
                    lastSavedUri = lastSavedUri,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSaved = { spriteId, uri, fileName, scale, onResult ->
                        viewModel.saveImage(spriteId, uri, fileName, scale, onResult)
                    },
                    onDismiss = viewModel::closeTopDialog
                )

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