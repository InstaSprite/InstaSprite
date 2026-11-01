package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.domain.tool.DrawingSymmetry
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun SymmetryOptions(symmetry: DrawingSymmetry, onEvent: (ToolSelectorEvent) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.pixelDp)) {
        SymmetryToggle(
            R.drawable.ic_hflip,
            stringResource(R.string.mirror_left_right),
            symmetry.horizontal
        ) {
            onEvent(ToolSelectorEvent.ToggleHorizontalSymmetry)
        }
        SymmetryToggle(
            R.drawable.ic_vflip,
            stringResource(R.string.mirror_top_bottom),
            symmetry.vertical
        ) {
            onEvent(ToolSelectorEvent.ToggleVerticalSymmetry)
        }
    }
}

@Composable
private fun SymmetryToggle(icon: Int, description: String, active: Boolean, onToggle: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(24.pixelDp)
            .clip(MaterialTheme.shapes.small)
            .background(if (active) colors.green else colors.surfaceInset)
            .toggleable(value = active, role = Role.Checkbox, onValueChange = { onToggle() }),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(icon, contentDescription = description)
    }
}

@Preview(name = "Mirror drawing", showBackground = true)
@Composable
private fun SymmetryOptionsPreview() {
    InstaSpriteTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(6.pixelDp)) {
            SymmetryOptions(DrawingSymmetry(), {})
            SymmetryOptions(DrawingSymmetry(horizontal = true), {})
            SymmetryOptions(DrawingSymmetry(horizontal = true, vertical = true), {})
        }
    }
}
