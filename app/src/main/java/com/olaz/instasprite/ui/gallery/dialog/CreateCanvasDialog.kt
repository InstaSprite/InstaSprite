package com.olaz.instasprite.ui.gallery.dialog

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.olaz.instasprite.DrawingActivity
import com.olaz.instasprite.data.repository.loadDefaultColorPalette
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.dialog.InputDialog
import java.util.UUID


@Composable
fun CreateCanvasDialog(
    onDismiss: () -> Unit,
    onPaletteViewClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedPalette by remember { mutableStateOf<ColorPalette?>(null) }

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

            val intent = Intent(context, DrawingActivity::class.java).apply {
                putExtra(DrawingActivity.EXTRA_CANVAS_WIDTH, width)
                putExtra(DrawingActivity.EXTRA_CANVAS_HEIGHT, height)
                putExtra(DrawingActivity.EXTRA_SPRITE_ID, UUID.randomUUID().toString())
                putExtra(DrawingActivity.EXTRA_SPRITE_NAME, name)
            }

            onDismiss()
            context.startActivity(intent)
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
