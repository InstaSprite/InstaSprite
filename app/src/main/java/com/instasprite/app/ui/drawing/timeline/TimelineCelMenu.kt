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
import com.instasprite.app.ui.drawing.contract.TimelineEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
internal fun TimelineCelMenu(
    expanded: Boolean,
    enabled: Boolean,
    hasCel: Boolean,
    isLinked: Boolean,
    hasPreviousCel: Boolean,
    onDismiss: () -> Unit,
    onEvent: (TimelineEvent) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(AppTheme.colors.background)
    ) {
        val actions = listOf(
            Triple(R.string.animation_copy_cel, TimelineEvent.CopyPrevious, hasPreviousCel),
            Triple(R.string.animation_link_cel, TimelineEvent.LinkPrevious, hasPreviousCel),
            Triple(R.string.animation_unlink, TimelineEvent.Unlink, isLinked),
            Triple(R.string.animation_clear, TimelineEvent.ClearCel, hasCel)
        )
        actions.forEach { (label, event, available) ->
            if (event != TimelineEvent.Unlink || isLinked) {
                DropdownMenuItem(
                    text = { Text(stringResource(label)) },
                    enabled = enabled && available,
                    onClick = { onDismiss(); onEvent(event) }
                )
            }
        }
    }
}

@Preview(name = "Linked cel actions")
@Composable
private fun TimelineCelMenuPreview() {
    InstaSpriteTheme {
        TimelineCelMenu(true, true, true, true, true, {}, {})
    }
}
