package com.olaz.instasprite.ui.social.feed.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.olaz.instasprite.ui.components.dialog.CustomDialog

@Composable
fun DeletePostConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    CustomDialog(
        title = "Delete post",
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmButtonText = "Delete",
        dismissButtonText = "Cancel",
        content = {
            Text("Are you sure you want to delete this post?")
        }
    )
}
