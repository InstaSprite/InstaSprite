package com.olaz.instasprite.ui.drawing.dialog

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.dialog.CustomDialog
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import kotlinx.coroutines.launch

@Composable
fun LospecImportDialog(
    onDismiss: () -> Unit,
    onImportColorsFromLospecUrl: suspend (String) -> List<Color>,
    onImport: (List<Color>) -> Unit,
) {
    var paletteUrl by remember { mutableStateOf("") }
    var previewColors by remember { mutableStateOf<List<Color>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    CustomDialog(
        title = "Import from Lospec",
        onDismiss = onDismiss,
        onConfirm = {
            if (previewColors != null && previewColors!!.isNotEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = paletteUrl,
                    onValueChange = { newUrl ->
                        paletteUrl = newUrl
                    },
                    label = { Text("Lospec URL", color = CatppuccinUI.SelectedColor) },
                    placeholder = {
                        Text(
                            "https://lospec.com/palette-list/example",
                             color = CatppuccinUI.Subtext0Color,
                            style = CatppuccinTypography.bodyMedium
                        )
                    },
                    textStyle = CatppuccinTypography.bodyMedium,
                    singleLine = true,
                    colors = CatppuccinUI.OutlineTextFieldColors.colors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (previewColors != null) {
                    ColorPaletteView(
                        colors = previewColors!!,
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
                                val colors = onImportColorsFromLospecUrl(trimmedUrl)
                                if (colors.isEmpty()) {
                                    Toast.makeText(context, "No colors found", Toast.LENGTH_SHORT).show()
                                    previewColors = null
                                } else {
                                    previewColors = colors
                                    Toast.makeText(context, "Colors loaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                previewColors = null
                                Toast.makeText(context, "An error occurred while importing the palette", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.AccentButtonColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Fetch Palette", color = CatppuccinUI.TextColorDark)
                }
            }
        }
    )
}
