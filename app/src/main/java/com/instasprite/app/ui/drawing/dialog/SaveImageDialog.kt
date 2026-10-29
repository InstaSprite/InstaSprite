package com.instasprite.app.ui.drawing.dialog

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.dialog.SaveFileDialog

@Composable
fun SaveImageDialog(
    onDismiss: () -> Unit,
    folderUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: suspend (folderUri: Uri, fileName: String, scale: Int) -> Boolean,
    isSaving: Boolean = false,
    initialName: String = "Sprite"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf(initialName) }
    var scalePercent by remember { mutableStateOf("100") }

    SaveFileDialog(
        title = stringResource(R.string.export_image),
        isSaving = isSaving,
        fields = listOf(
            InputField(
                label = stringResource(R.string.filename),
                placeholder = "Sprite",
                keyboardType = KeyboardType.Text,
                suffix = ".png",
                validator = { it.isNotBlank() && '/' !in it && '\\' !in it },
                errorMessage = stringResource(R.string.invalid_export_filename),
                defaultValue = initialName
            ),
            InputField(
                label = stringResource(R.string.scale),
                placeholder = "100",
                keyboardType = KeyboardType.Number,
                suffix = "%",
                validator = { it.toIntOrNull() != null && it.toInt() in 25..20000 },
                errorMessage = stringResource(R.string.must_be_between_25_and_20000),
                defaultValue = "100"
            )
        ),
        lastSavedUri = folderUri,
        onFolderSelected = onFolderSelected,
        onSave = {
            if (!isSaving && folderUri != null) scope.launch {
                val scale = scalePercent.toIntOrNull() ?: 100
                val success = onSave(folderUri, "${fileName.removeSuffix(".png")}.png", scale)
                Toast.makeText(context,
                    if (success) R.string.export_saved else R.string.export_failed,
                    Toast.LENGTH_SHORT).show()
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