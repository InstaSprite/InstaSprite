package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

object EyedropperTool : Tool {
    override val icon: Int = R.drawable.ic_eyedropper_tool
    override val name: String = "Eyedropper"
    override val description: String = "Select a pixel color"
    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        val selectedColor = Color(canvas.getCompositedPixelAt(row, col))
        if (selectedColor != Color.Transparent) {
            canvas.selectColor(selectedColor)
        }
    }
}