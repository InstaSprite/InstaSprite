package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

object EyedropperTool : Tool {
    override val type = ToolType.Eyedropper
    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        val selectedColor = Color(canvas.getCompositedPixelAt(row, col))
        if (selectedColor != Color.Transparent) {
            canvas.selectColor(selectedColor)
        }
    }
}