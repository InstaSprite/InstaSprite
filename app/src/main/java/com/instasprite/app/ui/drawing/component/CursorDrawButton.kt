package com.instasprite.app.ui.drawing.component

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun CursorDrawButton(
    selectedTool: Tool,
    onPressed: () -> Unit,
    onReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor = if (isPressed) {
        AppTheme.colors.Foreground1Color
    } else {
        AppTheme.colors.Foreground0Color
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPressed()
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    onReleased()
                }
            }
    ) {
        Text(text = stringResource(R.string.use_tool), modifier = Modifier.align(Alignment.Center))
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        CursorDrawButton(
            selectedTool = PencilTool,
            onPressed = {},
            onReleased = {}
        )
    }
}
