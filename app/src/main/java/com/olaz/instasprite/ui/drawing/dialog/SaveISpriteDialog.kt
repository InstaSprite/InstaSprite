package com.olaz.instasprite.ui.drawing.dialog

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.components.dialog.SaveFileDialog
import kotlinx.coroutines.launch

@Composable
fun SaveISpriteDialog(
    onDismiss: () -> Unit,
    folderUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: suspend (folderUri: Uri, fileName: String) -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf("Sprite") }
    var isSaving by remember { mutableStateOf(false) }

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
        isSaving = isSaving,
        lastSavedUri = folderUri,
        onFolderSelected = onFolderSelected,
        onSave = {
            folderUri?.let { uri ->
                if (!isSaving) {
                    isSaving = true

                    scope.launch {
                        val success = onSave(uri, fileName)

                        isSaving = false

                        Toast.makeText(
                            context,
                            if (success) "$fileName saved successfully!" else "Failed to save",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (success) onDismiss()
                    }
                }
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ ->
            fileName = values.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Sprite"
        }
    )
}