package com.olaz.instasprite.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.olaz.instasprite.domain.dialog.Dialog
import com.olaz.instasprite.domain.model.SpriteWithMeta
import com.olaz.instasprite.ui.components.dialog.ConfirmationDialog
import com.olaz.instasprite.ui.gallery.dialog.RenameDialog
import com.olaz.instasprite.ui.gallery.dialog.SaveImageDialog
import com.olaz.instasprite.ui.gallery.dialog.SelectSortOptionDialog
import com.olaz.instasprite.ui.drawing.dialog.LoadISpriteDialog
import com.olaz.instasprite.ui.theme.CatppuccinUI


sealed interface GalleryDialog : Dialog {
    data class SaveImage(val sprite: SpriteWithMeta) : GalleryDialog
    data class Rename(val spriteId: String) : GalleryDialog
    data class DeleteSpriteConfirm(val spriteName: String, val spriteId: String) : GalleryDialog
    data object SelectSortOption : GalleryDialog
    data object LoadISprite : GalleryDialog
}

@Composable
fun GalleryScreenDialogs(
    dialogState: List<GalleryDialog>,
    viewModel: GalleryViewModel
) {
    var lastSavedUri = viewModel.lastSavedLocation.collectAsState().value
    val spriteListOrder = viewModel.spriteListOrder.collectAsState().value

    LaunchedEffect(Unit) {
        lastSavedUri = viewModel.getLastSavedLocation()
    }

    dialogState.forEach { dialog ->
        when (dialog) {
            is GalleryDialog.DeleteSpriteConfirm ->
                ConfirmationDialog(
                    title = "Delete sprite",
                    text = "Are you sure you want to delete",
                    highlightText = dialog.spriteName,
                    confirmButtonText = "Delete",
                    dismissButtonText = "Cancel",
                    highlightTextColor = CatppuccinUI.DismissButtonColor,
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

            is GalleryDialog.SelectSortOption ->
                SelectSortOptionDialog(
                    onSortOrderSelected = { sortOrder ->
                        viewModel.setSpriteListOrder(order = sortOrder)
                        viewModel.saveSortSetting(sortOrder)
                    },
                    spriteListOrder = spriteListOrder,
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