package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

interface Tool {
    val icon: Int
    val name: String
    val description: String

    fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color): Unit

    fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int): Unit {
        apply(canvas, row, col, color)
    }
}