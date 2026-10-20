package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
internal fun TimelineFrameDeleteMenu(
    expanded: Boolean,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(AppTheme.colors.BackgroundColor)
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.animation_delete)) },
            enabled = enabled,
            onClick = { onDismiss(); onDelete() }
        )
    }
}

@Preview
@Composable
private fun TimelineFrameDeleteMenuPreview() {
    InstaSpriteTheme {
        TimelineFrameDeleteMenu(true, true, {}, {})
    }
}
