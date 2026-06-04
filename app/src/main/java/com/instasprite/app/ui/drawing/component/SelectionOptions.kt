package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.lazy.LazyListScope
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.drawing.DrawingScreenEvent
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent

fun LazyListScope.SelectionOptions(
    tool: Tool,
    event: DrawingScreenEvent
) {
    item {
        SelectionModeSelector(
            selectedTool = tool,
            onSelectionToolSelected = { selected ->
                event.onToolSelectorEvent(ToolSelectorEvent.SelectTool(selected))
            }
        )
    }
}
