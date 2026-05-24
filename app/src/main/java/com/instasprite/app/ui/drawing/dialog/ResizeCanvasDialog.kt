package com.instasprite.app.ui.drawing.dialog


import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
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
                label = stringResource(R.string.width),
                placeholder = stringResource(R.string.width),
                suffix = stringResource(R.string.px),
                defaultValue = currentCanvasWidth.toString(),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = stringResource(R.string.must_be_between_1_and_1024)
            ),
            InputField(
                label = stringResource(R.string.height),
                placeholder = stringResource(R.string.height),
                suffix = stringResource(R.string.px),
                defaultValue = currentCanvasHeight.toString(),
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = "Must be between 1 and 1024"
            )
        ),
        onDismiss = onDismiss,
        onConfirm = {
            onResize(it[0].toInt(), it[1].toInt())
            onDismiss()
        },
    )
}