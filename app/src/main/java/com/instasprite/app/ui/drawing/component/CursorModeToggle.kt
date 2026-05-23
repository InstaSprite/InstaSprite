package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun CursorModeToggle(
    isCursorMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isCursorMode) {
        AppTheme.colors.LinkColor
    } else {
        Color.Transparent
    }

    IconButton(
        onClick = onToggle,
        colors = IconButtonColors(
            containerColor = containerColor,
            contentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.size(32.dp)
    ) {
        PixelIcon(
            icon = R.drawable.ic_pencil_tool,
            contentDescription = stringResource(R.string.cursor_mode),
        )
    }
}

@Preview
@Composable
private fun PreviewOff() {
    InstaSpriteTheme {
        CursorModeToggle(
            isCursorMode = false,
            onToggle = {}
        )
    }
}

@Preview
@Composable
private fun PreviewOn() {
    InstaSpriteTheme {
        CursorModeToggle(
            isCursorMode = true,
            onToggle = {}
        )
    }
}
