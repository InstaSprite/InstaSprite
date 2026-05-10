package com.olaz.instasprite.ui.palette.dialogs

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.dialog.InputDialog
import com.olaz.instasprite.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun FileImportDialog(
    onDismiss: () -> Unit,
    onImportPaletteFromFile: suspend (uri: Uri) -> List<Color>,
    onImport: (List<Color>) -> Unit,
) {
    val context = LocalContext.current
    var previewColors by remember { mutableStateOf<List<Color>?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    selectedFileName = cursor.getString(nameIndex)
                }

                scope.launch {
                    try {
                        val colors = onImportPaletteFromFile(it)
                        if (colors.isEmpty()) {
                            Toast.makeText(context, "No colors found in file", Toast.LENGTH_SHORT)
                                .show()
                            previewColors = null
                        } else {
                            previewColors = colors
                            Toast.makeText(
                                context,
                                "Colors loaded successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        previewColors = null
                        Toast.makeText(
                            context,
                            "An error occurred while importing the palette",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    )

    InputDialog(
        title = "Import from File",
        fields = listOf(),
        onDismiss = onDismiss,
        onConfirm = {
            if (previewColors != null && previewColors!!.isNotEmpty()) {
                onImport(previewColors!!)
                Toast.makeText(context, "Palette imported successfully!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } else {
                Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
            }
        },
        confirmButtonText = "Import",
        dismissButtonText = "Back",
        extraTopContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { filePickerLauncher.launch(arrayOf("text/plain")) }
            ) {
                OutlinedTextField(
                    value = selectedFileName ?: "No file selected",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.import_location), color = AppTheme.colors.SelectedColor) },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.choose_file),
                            tint = AppTheme.colors.LinkColor,
                            modifier = Modifier.clickable {
                                filePickerLauncher.launch(arrayOf("text/plain"))
                            }
                        )
                    },
                    colors = AppTheme.colors.outlineTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        extraBottomContent = {
            if (previewColors != null) {
                ColorPaletteView(
                    colors = previewColors!!,
                    config = ColorPaletteConfig(isInteractive = false)
                )
            }
        }
    )
}
