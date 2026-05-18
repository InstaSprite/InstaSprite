package com.instasprite.app.ui.drawing.contract

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.Tool

data class PixelCanvasState(
    val width: Int = 0,
    val height: Int = 0,
    val drawVersion: Long = 0,
    val overlayVersion: Long = 0,
    val layers: List<Layer> = emptyList(),
    val activeLayerId: String = "",
    val selectedTool: Tool? = null,
    val selectionState: SelectionState? = null
)

sealed interface PixelCanvasEvent {
    data class OnTapAt(val x: Int, val y: Int) : PixelCanvasEvent
    data class OnStrokeStart(val x: Int, val y: Int, val zoomScale: Float = 1f) : PixelCanvasEvent
    data class OnStrokeMove(val x: Int, val y: Int, val zoomScale: Float = 1f) : PixelCanvasEvent
    data object OnStrokeEnd : PixelCanvasEvent
    data object OnStrokeCancel : PixelCanvasEvent
    data object ClearSelection : PixelCanvasEvent
    data object InvertSelection : PixelCanvasEvent
}