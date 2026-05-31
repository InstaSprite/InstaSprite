package com.instasprite.app.ui.components.dialog

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.getFullPathFromTreeUri

@Composable
fun SaveFileDialog(
    title: String,
    fields: List<InputField>,
    lastSavedUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onValuesChanged: ((values: List<String>, folderUri: Uri?) -> Unit)? = null,
    isSaving: Boolean = false
) {
    val context = LocalContext.current
    var folderUri by remember(lastSavedUri) { mutableStateOf<Uri?>(lastSavedUri) }

    LaunchedEffect(lastSavedUri) {
        if (lastSavedUri != null && folderUri == null) {
            folderUri = lastSavedUri
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                folderUri = it
                onFolderSelected(it)
            }
        }
    )

    val displayPath = folderUri?.let { getFullPathFromTreeUri(it) } ?: "Tap to select folder"

    InputDialog(
        title = title,
        fields = fields,
        confirmButtonText = stringResource(R.string.save),
        isLoading = isSaving,
        onDismiss = onDismiss,
        onConfirm = { values ->
            if (folderUri == null) {
                Toast.makeText(context, context.getString(R.string.choose_folder), Toast.LENGTH_SHORT).show()
            } else {
                onValuesChanged?.invoke(values, folderUri)
                onSave()
//                onDismiss()
            }
        },
        extraTopContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { folderPickerLauncher.launch(null) }
            ) {
                OutlinedTextField(
                    value = displayPath,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.save_location), color = AppTheme.colors.SelectedColor) },
                    supportingText = {},
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        PixelIcon(
                            icon = R.drawable.ic_home,
                            contentDescription = stringResource(R.string.choose_folder),
                            tint = AppTheme.colors.LinkColor,
                        )
                    },
                    colors = AppTheme.colors.outlineTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
@Preview
fun SaveFileDialogPreview() {
    InstaSpriteTheme {
        SaveFileDialog(
            title = "Title",
            fields = listOf(
                InputField(
                    label = "Label 1",
                    placeholder = "Placeholder 1",
                    keyboardType = KeyboardType.Text,
                    suffix = "Suffix 1",
                    validator = { it.isNotBlank() },
                ),
                InputField(
                    label = "Label 2",
                    placeholder = "Placeholder 2",
                    keyboardType = KeyboardType.Number,
                    validator = { it.toIntOrNull() != null },
                    errorMessage = "Must be a number"
                )
            ),
            lastSavedUri = null,
            onFolderSelected = {},
            onSave = {},
            onDismiss = {}
        )
    }
}