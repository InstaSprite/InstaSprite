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
import com.instasprite.app.R
import com.instasprite.app.domain.export.AnimationExportFormat
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.ui.components.dialog.SaveFileDialog
import kotlinx.coroutines.launch

@Composable
fun SaveAnimationDialog(
    format: AnimationExportFormat,
    sprite: Sprite,
    onDismiss: () -> Unit,
    folderUri: Uri?,
    isSaving: Boolean,
    onFolderSelected: (Uri) -> Unit,
    onSave: suspend (Uri, String, Int, Int) -> Boolean,
    initialName: String = "Sprite"
) {
    val frameCount = sprite.animation?.frames?.size ?: 1
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialName) }
    val sheet = format == AnimationExportFormat.SPRITE_SHEET
    var columns by remember(frameCount) {
        mutableStateOf(
            kotlin.math.ceil(
                kotlin.math.sqrt(
                    frameCount.toDouble()
                )
            ).toInt().toString()
        )
    }
    var scale by remember { mutableStateOf("100") }
    val fields = listOf(
        InputField(
            label = stringResource(R.string.filename),
            defaultValue = name,
            suffix = ".${format.extension}",
            validator = { it.isNotBlank() && '/' !in it && '\\' !in it },
            errorMessage = stringResource(R.string.invalid_export_filename)
        ),
        InputField(
            label = stringResource(R.string.scale), defaultValue = scale,
            suffix = "%", keyboardType = KeyboardType.Number,
            validator = { it.toIntOrNull()?.let { value -> value in 25..20000 } == true },
            errorMessage = stringResource(R.string.must_be_between_25_and_20000)
        )
    ) + if (sheet) listOf(
        InputField(
            label = stringResource(R.string.sheet_columns), defaultValue = columns,
            keyboardType = KeyboardType.Number,
            validator = { it.toIntOrNull()?.let { value -> value in 1..frameCount } == true },
            errorMessage = stringResource(R.string.sheet_columns_range, frameCount)
        )
    ) else emptyList()
    SaveFileDialog(
        title = stringResource(if (sheet) R.string.export_sprite_sheet else R.string.export_gif),
        fields = fields,
        scrollable = true,
        preview = {
            AnimationExportPreviewView(
                sprite, format,
                scale.toIntOrNull()?.takeIf { it in 25..20000 },
                if (sheet) columns.toIntOrNull()?.takeIf { it in 1..frameCount } else 1)
        },
        lastSavedUri = folderUri, onFolderSelected = onFolderSelected, isSaving = isSaving,
        onSave = {
            if (!isSaving && folderUri != null) scope.launch {
                val success =
                    onSave(folderUri, name, columns.toIntOrNull() ?: 1, scale.toIntOrNull() ?: 100)
                Toast.makeText(
                    context,
                    if (success) R.string.export_saved else R.string.export_failed,
                    Toast.LENGTH_SHORT
                ).show()
                if (success) onDismiss()
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ ->
            name = values.firstOrNull().orEmpty()
            scale = values.getOrNull(1).orEmpty()
            if (sheet) columns = values.getOrNull(2).orEmpty()
        }
    )
}
