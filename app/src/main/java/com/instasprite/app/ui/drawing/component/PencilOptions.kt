package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.graphics.Color
import com.instasprite.app.ui.drawing.DrawingScreenEvent
import com.instasprite.app.ui.drawing.DrawingScreenState
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent

fun LazyListScope.PencilOptions(
    uiState: DrawingScreenState,
    event: DrawingScreenEvent,
    toolSize: Int,
    onToolSizeChange: (Int) -> Unit,
    paletteColors: List<Color> = emptyList(),
    activeColor: Color = Color.Black
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
        DitherOptions(uiState.dither, activeColor, paletteColors, event.onToolSelectorEvent)
    }
    item {
        PixelPerfectOption(
            isActive = uiState.isPixelPerfect,
            onToggle = { event.onToolSelectorEvent(ToolSelectorEvent.TogglePixelPerfect) }
        )
    }
}
