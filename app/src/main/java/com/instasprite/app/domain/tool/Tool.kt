package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

interface Tool {
    val type: ToolType
    val icon: Int get() = type.icon
    val nameRes: Int get() = type.nameRes
    val descriptionRes: Int get() = type.descriptionRes

    fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color)

    fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {
        apply(canvas, row, col, color)
    }
}
