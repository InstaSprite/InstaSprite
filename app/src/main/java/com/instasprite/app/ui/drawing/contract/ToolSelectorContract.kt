package com.instasprite.app.ui.drawing.contract

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.tool.DitherPattern
import com.instasprite.app.domain.tool.ToolType

sealed interface ToolSelectorEvent {
    data class SetDitherPattern(val pattern: DitherPattern) : ToolSelectorEvent
    data class SetDitherBackground(val color: Color) : ToolSelectorEvent
    data class SelectTool(val tool: ToolType) : ToolSelectorEvent
    data object Undo : ToolSelectorEvent
    data object Redo : ToolSelectorEvent

    data object OpenSaveGifDialog : ToolSelectorEvent
    data object OpenSaveImageDialog : ToolSelectorEvent
    data object OpenSaveISpriteDialog : ToolSelectorEvent
    data object OpenLoadISpriteDialog : ToolSelectorEvent
    data object OpenDrawingSettingsDialog : ToolSelectorEvent
    data object OpenDitherColorWheel : ToolSelectorEvent
    data object ToggleAppendSelectionMode : ToolSelectorEvent
    data object TogglePixelPerfect : ToolSelectorEvent
    data class SetSymmetryAxisX(val position: Float) : ToolSelectorEvent
    data class SetSymmetryAxisY(val position: Float) : ToolSelectorEvent
    data object ToggleHorizontalSymmetry : ToolSelectorEvent
    data object ToggleVerticalSymmetry : ToolSelectorEvent
}
