package com.instasprite.app.ui.drawing.dialog

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.instasprite.app.domain.export.AnimationExportFormat
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.dialog.SaveFileDialog
import kotlinx.coroutines.launch

@Composable
fun SaveAnimationDialog(format: AnimationExportFormat, frameCount: Int, onDismiss: () -> Unit, folderUri: Uri?, isSaving: Boolean,
    onFolderSelected: (Uri) -> Unit, onSave: suspend (Uri, String, Int) -> Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("Sprite") }
    val sheet = format == AnimationExportFormat.SPRITE_SHEET
    var columns by remember(frameCount) { mutableStateOf(kotlin.math.ceil(kotlin.math.sqrt(frameCount.toDouble())).toInt().toString()) }
    val fields = listOf(InputField(label = stringResource(R.string.filename), defaultValue = name,
        suffix = ".${format.extension}", validator = { it.isNotBlank() && '/' !in it && '\\' !in it },
        errorMessage = stringResource(R.string.invalid_export_filename))) + if (sheet) listOf(
            InputField(label = stringResource(R.string.sheet_columns), defaultValue = columns,
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull()?.let { value -> value in 1..frameCount } == true },
                errorMessage = stringResource(R.string.sheet_columns_range, frameCount))) else emptyList()
    SaveFileDialog(
        title = stringResource(if (sheet) R.string.export_sprite_sheet else R.string.export_gif),
        fields = fields,
        note = stringResource(if (sheet) R.string.sheet_format_note else R.string.gif_format_note),
        lastSavedUri = folderUri, onFolderSelected = onFolderSelected, isSaving = isSaving,
        onSave = {
            if (!isSaving && folderUri != null) scope.launch {
                val success = onSave(folderUri, name, columns.toIntOrNull() ?: 1)
                Toast.makeText(context, if (success) R.string.export_saved else R.string.export_failed, Toast.LENGTH_SHORT).show()
                if (success) onDismiss()
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ ->
            name = values.firstOrNull().orEmpty()
            if (sheet) columns = values.getOrNull(1).orEmpty()
        }
    )
}
