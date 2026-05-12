package com.instasprite.app.ui.drawing.dialog


import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.dialog.InputDialog


@Composable
fun ResizeCanvasDialog(
    onDismiss: () -> Unit,
    currentCanvasWidth: Int,
    currentCanvasHeight: Int,
    onResize: (Int, Int) -> Unit,
) {
    InputDialog(
        title = "Resize canvas",
        fields = listOf(
            InputField(
                label = "Width",
                placeholder = "Width",
                suffix = "px",
                defaultValue = currentCanvasWidth.toString(),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 },
                errorMessage = "Must be a number larger than 0"
            ),
            InputField(
                label = "Height",
                placeholder = "Height",
                suffix = "px",
                defaultValue = currentCanvasHeight.toString(),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 },
                errorMessage = "Must be a number larger than 0"
            )
        ),
        onDismiss = onDismiss,
        onConfirm = {
            onResize(it[0].toInt(), it[1].toInt())
            onDismiss()
        },
    )
}