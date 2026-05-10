package com.olaz.instasprite.ui.drawing.component

import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.theme.AppTheme
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

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
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pencil_tool),
            contentDescription = stringResource(R.string.cursor_mode),
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
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
