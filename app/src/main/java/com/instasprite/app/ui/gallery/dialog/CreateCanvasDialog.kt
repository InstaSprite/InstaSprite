package com.instasprite.app.ui.gallery.dialog


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import com.instasprite.app.data.repository.loadDefaultColorPalette
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.ColorPaletteConfig
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.ui.components.dialog.InputDialog



@Composable
fun CreateCanvasDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit,
    onPaletteViewClick: () -> Unit = {},
    selectedPalette: ColorPalette? = null
) {
    val context = LocalContext.current

    InputDialog(
        title = "New canvas",
        fields = listOf(
            InputField(
                label = "Name",
                placeholder = "Untitled",
                defaultValue = "Untitled",
                keyboardType = KeyboardType.Text,
                validator = { it.length <= 20 },
                errorMessage = "Must be less than 20 characters"
            ),
            InputField(
                label = "Width",
                placeholder = "16",
                defaultValue = "16",
                suffix = "px",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toIntOrNull() != 0 },
                errorMessage = "Must be a number greater than 0"
            ),
            InputField(
                label = "Height",
                placeholder = "16",
                defaultValue = "16",
                suffix = "px",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toIntOrNull() != 0 },
                errorMessage = "Must be a number greater than 0"
            )
        ),
        onDismiss = onDismiss,
        confirmButtonText = "Create",
        onConfirm = { values ->
            val name = values[0]
            val width = values[1].toInt()
            val height = values[2].toInt()

            onConfirm(name, width, height)
            onDismiss()
        }, extraBottomContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var palette = loadDefaultColorPalette(LocalContext.current)
                    if (selectedPalette != null) palette = selectedPalette!!.colors

                    ColorPaletteView(
                        colors = palette,
                        config = ColorPaletteConfig(
                            isInteractive = false
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(true, onClick = onPaletteViewClick)
                    )
                }
            }
        })
}
