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
fun SaveImageDialog(
    onDismiss: () -> Unit,
    folderUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: (folderUri: Uri, fileName: String, scale: Int) -> Boolean
) {
    val context = LocalContext.current
    var fileName by remember { mutableStateOf("Sprite") }
    var scalePercent by remember { mutableStateOf("100") }

    SaveFileDialog(
        title = "Export Image",
        fields = listOf(
            InputField(
                label = "Name",
                placeholder = "Sprite",
                keyboardType = KeyboardType.Text,
                suffix = ".png",
                validator = { it.isNotBlank() },
                errorMessage = "Name cannot be blank",
                defaultValue = "Sprite"
            ),
            InputField(
                label = "Scale",
                placeholder = "100",
                keyboardType = KeyboardType.Number,
                suffix = "%",
                validator = { it.toIntOrNull() != null && it.toInt() in 25..20000 },
                errorMessage = "Must be a number between 25 and 20000",
                defaultValue = "100"
            )
        ),
        lastSavedUri = folderUri,
        onFolderSelected = onFolderSelected,
        onSave = {
            folderUri?.let { uri ->
                val scale = scalePercent.toIntOrNull()?.coerceIn(25, 20000) ?: 100
                val success = onSave(uri, "$fileName.png", scale)
                Toast.makeText(
                    context,
                    if (success) "Image saved successfully!" else "Failed to save image",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) onDismiss()
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ ->
            fileName = values.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Sprite"
            scalePercent = values.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "100"
        }
    )
}