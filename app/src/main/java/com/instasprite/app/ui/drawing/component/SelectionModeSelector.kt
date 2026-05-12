package com.instasprite.app.ui.drawing.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.selection.LassoSelectionTool
import com.instasprite.app.domain.tool.selection.MagicWandTool
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SelectionModeSelector(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedTool: Tool,
    onSelectionToolSelected: (Tool) -> Unit
) {
    val selectionTools = listOf(
        RectangleSelectionTool,
        LassoSelectionTool,
        MagicWandTool
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
                .background(AppTheme.colors.BackgroundColor),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            selectionTools.forEach { tool ->
                TextButton(
                    onClick = { onSelectionToolSelected(tool) },
                ) {
                    Text(
                        text = tool.name,
                        color = if (selectedTool == tool) AppTheme.colors.AccentButtonColor else AppTheme.colors.TextColorLight
                    )
                }
            }
        }
    }
}
