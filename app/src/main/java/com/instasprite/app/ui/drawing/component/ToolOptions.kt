package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.lazy.LazyListScope
import com.instasprite.app.domain.tool.EraserTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.ui.drawing.DrawingScreenEvent
import com.instasprite.app.ui.drawing.DrawingScreenState

fun LazyListScope.toolOptions(
    tool: Tool,
    uiState: DrawingScreenState,
    event: DrawingScreenEvent,
    toolSize: Int,
    onToolSizeChange: (Int) -> Unit
) {
    when (tool) {
        is PencilTool -> PencilOptions(uiState, event, toolSize, onToolSizeChange)
        is EraserTool -> EraserOptions(uiState, event, toolSize, onToolSizeChange)
        is ShapeTool -> ShapeOptions(tool, event, toolSize, onToolSizeChange)
        is SelectionTool -> SelectionOptions(tool as Tool, event)
    }
}
