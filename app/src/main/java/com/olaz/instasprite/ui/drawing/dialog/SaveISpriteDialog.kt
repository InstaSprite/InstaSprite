package com.olaz.instasprite.ui.drawing.dialog

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.components.dialog.SaveFileDialog

@Composable
fun SaveISpriteDialog(
    onDismiss: () -> Unit,
    folderUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: (folderUri: Uri, fileName: String) -> Boolean
) {
    val context = LocalContext.current
    var fileName by remember { mutableStateOf("Sprite") }

    SaveFileDialog(
        title = "Save ISprite",
        fields = listOf(
            InputField(
                label = "Name",
                placeholder = "Sprite",
                keyboardType = KeyboardType.Text,
                suffix = ".isprite",
                validator = { it.isNotBlank() },
                errorMessage = "Name cannot be blank",
                defaultValue = "Sprite"
            ),
        ),
        lastSavedUri = folderUri,
        onFolderSelected = onFolderSelected,
        onSave = {
            folderUri?.let { uri ->
                val success = onSave(uri, fileName)
                Toast.makeText(
                    context,
                    if (success) "$fileName saved successfully!" else "Failed to file",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) onDismiss()
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ ->
            fileName = values.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Sprite"
        }
    )
}