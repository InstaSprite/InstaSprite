package com.instasprite.app.ui.drawing.contract

import com.instasprite.app.domain.tool.Tool

sealed interface ToolSelectorEvent {
    data class SelectTool(val tool: Tool) : ToolSelectorEvent
    data object Undo : ToolSelectorEvent
    data object Redo : ToolSelectorEvent

    data object OpenSaveImageDialog : ToolSelectorEvent
    data object OpenSaveISpriteDialog : ToolSelectorEvent
    data object OpenLoadISpriteDialog : ToolSelectorEvent
    data object ToggleAppendSelectionMode : ToolSelectorEvent
}