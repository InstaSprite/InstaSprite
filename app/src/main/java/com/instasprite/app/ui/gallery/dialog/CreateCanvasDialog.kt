package com.instasprite.app.ui.gallery.dialog


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
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
        title = stringResource(R.string.new_canvas),
        fields = listOf(
            InputField(
                label = stringResource(R.string.name),
                placeholder = stringResource(R.string.untitled),
                defaultValue = stringResource(R.string.untitled),
                keyboardType = KeyboardType.Text,
                validator = { it.length <= 20 },
                errorMessage = stringResource(R.string.must_be_less_than_20_chars)
            ),
            InputField(
                label = stringResource(R.string.width),
                placeholder = "16",
                defaultValue = "16",
                suffix = stringResource(R.string.px),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = stringResource(R.string.must_be_between_1_and_1024)
            ),
            InputField(
                label = stringResource(R.string.height),
                placeholder = "16",
                defaultValue = "16",
                suffix = stringResource(R.string.px),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = stringResource(R.string.must_be_between_1_and_1024)
            )
        ),
        onDismiss = onDismiss,
        confirmButtonText = stringResource(R.string.create),
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
