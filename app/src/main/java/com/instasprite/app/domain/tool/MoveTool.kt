package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

object MoveTool : Tool {
    override val icon: Int = R.drawable.ic_move_tool
    override val name: String = "Move"
    override val description: String = "Move the canvas"

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        // Just declare for implement Tool interface, logic is in viewModel
    }
}