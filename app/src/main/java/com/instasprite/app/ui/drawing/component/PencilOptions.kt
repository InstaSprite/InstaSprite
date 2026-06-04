package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.instasprite.app.ui.drawing.DrawingScreenEvent
import com.instasprite.app.ui.drawing.DrawingScreenState
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.utils.pixelDp

fun LazyListScope.PencilOptions(
    uiState: DrawingScreenState,
    event: DrawingScreenEvent,
    toolSize: Int,
    onToolSizeChange: (Int) -> Unit
) {
    item {
        BrushShapeSelector(
            selectedShape = uiState.brushShape,
            onShapeSelected = event.onBrushShapeChange
        )
    }
    item {
        ToolSizeOption(
            toolSize = toolSize,
            onToolSizeChange = onToolSizeChange
        )
    }
    item {
        PixelPerfectOption(
            isActive = uiState.isPixelPerfect,
            onToggle = { event.onToolSelectorEvent(ToolSelectorEvent.TogglePixelPerfect) }
        )
    }
}
