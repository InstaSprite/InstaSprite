package com.olaz.instasprite.ui.gallery.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.components.dialog.InputDialog

@Composable
fun RenameDialog(
    spriteId: String,
    onSpriteRename: (spriteId: String, newName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    InputDialog(
        title = "Rename",
        fields = listOf(
            InputField(
                label = "Name",
                placeholder = "Enter name",
                keyboardType = KeyboardType.Text,
                validator = { it.isNotBlank() },
                errorMessage = "Name cannot be blank"
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