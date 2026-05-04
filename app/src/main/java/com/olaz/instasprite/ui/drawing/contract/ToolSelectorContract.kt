package com.olaz.instasprite.ui.drawing.contract

import com.olaz.instasprite.domain.tool.Tool

sealed interface ToolSelectorEvent {
    data class SelectTool(val tool: Tool) : ToolSelectorEvent
    data object Undo : ToolSelectorEvent
    data object Redo : ToolSelectorEvent

    data object OpenSaveImageDialog : ToolSelectorEvent
    data object OpenSaveISpriteDialog : ToolSelectorEvent
    data object OpenLoadISpriteDialog : ToolSelectorEvent
    data object ToggleAppendSelectionMode : ToolSelectorEvent
}