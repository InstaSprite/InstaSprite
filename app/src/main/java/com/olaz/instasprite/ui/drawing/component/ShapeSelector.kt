package com.olaz.instasprite.ui.drawing.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.tool.shape.CircleTool
import com.olaz.instasprite.domain.tool.shape.DiamondTool
import com.olaz.instasprite.domain.tool.shape.LineTool
import com.olaz.instasprite.domain.tool.shape.OvalTool
import com.olaz.instasprite.domain.tool.shape.RectangleTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun ShapeSelector(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedTool: Tool,
    onShapeSelected: (Tool) -> Unit
) {
    val shapeTools = listOf(
        LineTool,
        RectangleTool,
        CircleTool,
        OvalTool,
        DiamondTool
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = ExitTransition.None,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CatppuccinUI.BackgroundColor)
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            shapeTools.forEach { tool ->
                ToolItem(
                    iconResourceId = tool.icon,
                    contentDescription = tool.description,
                    selected = selectedTool == tool,
                    onClick = { onShapeSelected(tool) }
                )
            }
        }
    }
}
