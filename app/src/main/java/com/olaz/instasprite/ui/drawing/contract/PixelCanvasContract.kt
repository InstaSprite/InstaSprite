package com.olaz.instasprite.ui.drawing.contract

import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.tool.Tool

data class PixelCanvasState(
    val width: Int = 0,
    val height: Int = 0,
    val drawVersion: Long = 0,
    val overlayVersion: Long = 0,
    val layers: List<Layer> = emptyList(),
    val activeLayerId: String = "",
    val selectedTool: Tool? = null
)

sealed interface PixelCanvasEvent {
    data class OnTapAt(val x: Int, val y: Int) : PixelCanvasEvent
    data class OnStrokeStart(val x: Int, val y: Int) : PixelCanvasEvent
    data class OnStrokeMove(val x: Int, val y: Int) : PixelCanvasEvent
    data object OnStrokeEnd : PixelCanvasEvent
    data object OnStrokeCancel : PixelCanvasEvent
}