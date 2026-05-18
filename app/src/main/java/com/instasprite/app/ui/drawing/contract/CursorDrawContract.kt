package com.instasprite.app.ui.drawing.contract

import androidx.compose.ui.graphics.Color

data class CursorState(
    val cursorX: Float = 0f,
    val cursorY: Float = 0f,
    val isVisible: Boolean = false,
    val isDrawing: Boolean = false,
    val previewColor: Color? = null
) {
    val gridX: Int get() = cursorX.toInt()
    val gridY: Int get() = cursorY.toInt()
}

sealed interface CursorDrawEvent {
    data class ToggleCursorMode(val cursorX: Float = -1f, val cursorY: Float = -1f) : CursorDrawEvent
    data class MoveCursor(val cursorX: Float, val cursorY: Float) : CursorDrawEvent
    data object DrawButtonPressed : CursorDrawEvent
    data object DrawButtonReleased : CursorDrawEvent
}
