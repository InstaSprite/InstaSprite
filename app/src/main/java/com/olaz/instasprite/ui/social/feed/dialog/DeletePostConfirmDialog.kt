package com.olaz.instasprite.ui.social.feed.dialog

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

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
            Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_post))
        }
    )
}
