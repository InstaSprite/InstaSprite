package com.olaz.instasprite.ui.components.dialog

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.getFullPathFromTreeUri

@Composable
fun SaveFileDialog(
    title: String,
    fields: List<InputField>,
    lastSavedUri: Uri?,
    onFolderSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onValuesChanged: ((values: List<String>, folderUri: Uri?) -> Unit)? = null
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
        confirmButtonText = "Save",
        onDismiss = onDismiss,
        onConfirm = { values ->
            if (folderUri == null) {
                Toast.makeText(context, "Please select a folder", Toast.LENGTH_SHORT).show()
            } else {
                onValuesChanged?.invoke(values, folderUri)
                onSave()
                onDismiss()
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
                    label = { Text("Save Location", color = CatppuccinUI.SelectedColor) },
                    supportingText = {},
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Choose Folder",
                        )
                    },
                    colors = CatppuccinUI.OutlineTextFieldColors.colors(),
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