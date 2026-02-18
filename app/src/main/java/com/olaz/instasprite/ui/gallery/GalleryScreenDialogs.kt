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
import com.olaz.instasprite.ui.theme.CatppuccinUI


sealed interface GalleryDialog : Dialog {
    data class SaveImage(val sprite: SpriteWithMeta) : GalleryDialog
    data class Rename(val spriteId: String) : GalleryDialog
    data class DeleteSpriteConfirm(val spriteName: String, val spriteId: String) : GalleryDialog
    data object SelectSortOption : GalleryDialog
}

@Composable
fun GalleryScreenDialogs(
    dialogState: List<GalleryDialog>,
    viewModel: GalleryViewModel
) {
    val context = LocalContext.current
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
                    text = "Are you sureyou::want to delete",
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
                    sprite = dialog.sprite.sprite,
                    lastSavedUri = lastSavedUri,
                    onFolderSelected = viewModel::setLastSavedLocation,
                    onSaved = { sprite, uri, fileName, scale ->
                        viewModel.saveImage(context, sprite, uri, "$fileName.png", scale)
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
        }
    }
}