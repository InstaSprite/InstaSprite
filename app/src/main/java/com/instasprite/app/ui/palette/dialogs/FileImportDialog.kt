package com.instasprite.app.ui.palette.dialogs

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.ui.components.composable.ColorPaletteConfig
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.InputDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.noRippleClickable
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
                            Toast.makeText(context, context.getString(R.string.no_colors_found_in_file), Toast.LENGTH_SHORT)
                                .show()
                            previewColors = null
                        } else {
                            previewColors = colors
                            Toast.makeText(
                                context,
                                context.getString(R.string.colors_loaded_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        previewColors = null
                        Toast.makeText(
                            context,
                            context.getString(R.string.an_error_occurred_while_importing_the_palette),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    )

    InputDialog(
        title = stringResource(R.string.import_from_file),
        fields = listOf(),
        onDismiss = onDismiss,
        onConfirm = {
            if (previewColors != null && previewColors!!.isNotEmpty()) {
                onImport(previewColors!!)
                Toast.makeText(context, context.getString(R.string.palette_imported_successfully), Toast.LENGTH_SHORT).show()
                onDismiss()
            } else {
                Toast.makeText(context, context.getString(R.string.please_select_a_file_first), Toast.LENGTH_SHORT).show()
            }
        },
        confirmButtonText = stringResource(R.string.import_button),
        dismissButtonText = stringResource(R.string.back),
        extraTopContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { filePickerLauncher.launch(arrayOf("text/plain")) }
            ) {
                OutlinedTextField(
                    value = selectedFileName ?: stringResource(R.string.no_file_selected),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.import_location), color = AppTheme.colors.SelectedColor) },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        PixelIcon(
                            icon = R.drawable.ic_folder,
                            contentDescription = stringResource(R.string.choose_file),
                            tint = AppTheme.colors.LinkColor,
                            modifier = Modifier.noRippleClickable {
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
