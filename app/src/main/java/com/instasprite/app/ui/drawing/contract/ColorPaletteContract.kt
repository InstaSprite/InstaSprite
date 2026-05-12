package com.instasprite.app.ui.drawing.contract

import androidx.compose.ui.graphics.Color

data class ColorPaletteState(
    val colorPalette: List<Color>,
    val activeColor: Color,
    val recentColors: List<Color>,
)

sealed interface ColorPaletteEvent {
    data class SelectColor(val color: Color) : ColorPaletteEvent
    data object OpenColorWheelDialog : ColorPaletteEvent
}

sealed interface CanvasMenuEvent {
    data object RotateCanvas : CanvasMenuEvent
    data object HorizontalFlip : CanvasMenuEvent
    data object VerticalFlip : CanvasMenuEvent
    data object OpenResizeDialog : CanvasMenuEvent
}
