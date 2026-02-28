package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

data class PixelChange(val row: Int, val col: Int, val color: Int)

data class StrokeUpdate(
    val changes: List<PixelChange>,
    val isFullPreview: Boolean = false
)

interface StrokeTool : Tool {
    val commitsImmediately: Boolean

    fun beginStroke(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int): StrokeUpdate
    fun updateStroke(canvas: PixelCanvasUseCase, row: Int, col: Int): StrokeUpdate
    fun endStroke(): List<PixelChange>
    fun cancelStroke()
}
