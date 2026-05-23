package com.instasprite.app.ui.palette.dialogs

import com.instasprite.app.utils.pixelDp

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.ui.components.composable.ColorPaletteConfig
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun LospecImportDialog(
    onDismiss: () -> Unit,
    onImportColorsFromLospecUrl: suspend (String) -> ColorPalette?,
    onImport: (ColorPalette) -> Unit,
) {
    var paletteUrl by remember { mutableStateOf("") }
    var previewColors by remember { mutableStateOf<ColorPalette?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    CustomDialog(
        title = "Import from Lospec",
        onDismiss = onDismiss,
        onConfirm = {
            if (previewColors != null && previewColors!!.colors.isNotEmpty()) {
                onImport(previewColors!!)
                Toast.makeText(
                    context,
                    "Palette imported successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            } else {
                Toast.makeText(
                    context,
                    "Please fetch a palette first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        confirmButtonText = "Import",
        dismissButtonText = "Back",
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.pixelDp)
            ) {
                OutlinedTextField(
                    value = paletteUrl,
                    onValueChange = { newUrl ->
                        paletteUrl = newUrl
                    },
                    label = { Text(stringResource(R.string.lospec_url), color = AppTheme.colors.SelectedColor) },
                    placeholder = {
                        Text(
                            stringResource(R.string.https_lospec_com_palette_list_example),
                             color = AppTheme.colors.Subtext0Color,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    colors = AppTheme.colors.outlineTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                previewColors?.let {
                    ColorPaletteView(
                        colors = it.colors,
                        config = ColorPaletteConfig(isInteractive = false)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val trimmedUrl = paletteUrl.trim()

                            if (trimmedUrl.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter a Lospec URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            if (!trimmedUrl.contains("lospec.com") ||
                                !trimmedUrl.contains("palette-list")) {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid Lospec palette URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            try {
                                val palette = onImportColorsFromLospecUrl(trimmedUrl)
                                if (palette == null) {
                                    Toast.makeText(context, "No palette found", Toast.LENGTH_SHORT).show()
                                    previewColors = null
                                } else {
                                    previewColors = palette
                                    Toast.makeText(context, "Palette loaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                previewColors = null
                                Toast.makeText(context, "An error occurred while importing the palette", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.AccentButtonColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.fetch_palette), color = AppTheme.colors.TextColorDark)
                }
            }
        }
    )
}