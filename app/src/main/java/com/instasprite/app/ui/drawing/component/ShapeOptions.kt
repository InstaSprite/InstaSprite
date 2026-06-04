package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.ui.drawing.DrawingScreenEvent
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.utils.pixelDp

fun LazyListScope.ShapeOptions(
    tool: ShapeTool,
    event: DrawingScreenEvent,
    toolSize: Int,
    onToolSizeChange: (Int) -> Unit
) {
    item {
        ShapeSelector(
            selectedTool = tool,
            onShapeSelected = { selected ->
                event.onToolSelectorEvent(ToolSelectorEvent.SelectTool(selected))
            }
        )
    }
    item {
        ToolSizeOption(
            toolSize = toolSize,
            onToolSizeChange = onToolSizeChange,
            modifier = Modifier.padding(end = 6.pixelDp)
        )
    }
}
