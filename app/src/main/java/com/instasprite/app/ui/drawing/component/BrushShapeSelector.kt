package com.instasprite.app.ui.drawing.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun BrushShapeSelector(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedShape: BrushShape,
    onShapeSelected: (BrushShape) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = ExitTransition.None,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.BackgroundColor)
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BrushShape.entries.forEach { shape ->
                ToolItem(
                    iconResourceId = shape.icon,
                    contentDescription = shape.name,
                    selected = selectedShape == shape,
                    onClick = { onShapeSelected(shape) }
                )
            }
        }
    }
}
