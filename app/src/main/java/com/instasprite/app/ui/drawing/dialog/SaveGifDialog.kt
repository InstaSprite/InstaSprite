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
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.dialog.SaveFileDialog
import kotlinx.coroutines.launch

@Composable
fun SaveGifDialog(onDismiss: () -> Unit, folderUri: Uri?, isSaving: Boolean,
    onFolderSelected: (Uri) -> Unit, onSave: suspend (Uri, String) -> Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("Sprite") }
    SaveFileDialog(
        title = stringResource(R.string.export_gif),
        fields = listOf(InputField(label = stringResource(R.string.filename), defaultValue = name,
            suffix = ".gif", validator = { it.isNotBlank() && '/' !in it && '\\' !in it },
            errorMessage = stringResource(R.string.must_not_be_empty))),
        note = stringResource(R.string.gif_format_note),
        lastSavedUri = folderUri, onFolderSelected = onFolderSelected, isSaving = isSaving,
        onSave = {
            if (!isSaving && folderUri != null) scope.launch {
                val success = onSave(folderUri, name)
                Toast.makeText(context, if (success) R.string.export_saved else R.string.export_failed, Toast.LENGTH_SHORT).show()
                if (success) onDismiss()
            }
        },
        onDismiss = onDismiss,
        onValuesChanged = { values, _ -> name = values.firstOrNull().orEmpty() }
    )
}
