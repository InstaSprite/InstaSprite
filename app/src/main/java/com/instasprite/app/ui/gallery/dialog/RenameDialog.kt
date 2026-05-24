package com.instasprite.app.ui.gallery.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.dialog.InputDialog

@Composable
fun RenameDialog(
    spriteId: String,
    onSpriteRename: (spriteId: String, newName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    InputDialog(
        title = stringResource(R.string.rename),
        fields = listOf(
            InputField(
                label = stringResource(R.string.name),
                placeholder = stringResource(R.string.name),
                keyboardType = KeyboardType.Text,
                validator = { it.isNotBlank() },
                errorMessage = stringResource(R.string.must_not_be_empty)
            )
        ),
        onDismiss = onDismiss,
        onConfirm = { values ->
            val newName = values[0]
            onSpriteRename(spriteId, newName)
            onDismiss()
        }
    )
}