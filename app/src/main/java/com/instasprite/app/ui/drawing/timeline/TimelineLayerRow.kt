package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
internal fun TimelineLayerRow(
    layer: Layer,
    isActive: Boolean,
    onClick: () -> Unit,
    onVisibilityToggle: () -> Unit,
    onLockToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(16.pixelDp)
            .background(if (isActive) colors.BackgroundColor else colors.BackgroundColorDarker)
            .timelineGridBorder(colors.Foreground0Color)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(16.pixelDp)) {
            PixelIcon(
                if (layer.isVisible) R.drawable.ic_visible_on else R.drawable.ic_visible_off,
                contentDescription = stringResource(R.string.toggle_visibility),
                tint = if (layer.isVisible) colors.TextColorLight else colors.Subtext1Color
            )
        }
        IconButton(onClick = onLockToggle, modifier = Modifier.size(16.pixelDp)) {
            PixelIcon(
                if (layer.isLocked) R.drawable.ic_lock else R.drawable.ic_lock_unlock,
                contentDescription = stringResource(R.string.toggle_lock),
                tint = colors.TextColorLight
            )
        }
        if (layer.isClipping) {
            PixelIcon(
                R.drawable.ic_down_arrow,
                contentDescription = stringResource(R.string.clipping_layer),
                tint = colors.TextColorLight
            )
        }
        Text(
            layer.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
            color = colors.TextColorLight
        )
    }
}

@Preview(name = "Active layer", widthDp = 140)
@Composable
private fun TimelineLayerRowPreview() {
    var layer by remember { mutableStateOf(TimelinePreviewData.layers[1].copy(isClipping = true)) }
    InstaSpriteTheme {
        TimelineLayerRow(
            layer, true, {},
            onVisibilityToggle = { layer = layer.copy(isVisible = !layer.isVisible) },
            onLockToggle = { layer = layer.copy(isLocked = !layer.isLocked) })
    }
}
