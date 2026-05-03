package com.olaz.instasprite.domain.tool.selection

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

object SelectionToolPlaceholder : Tool {
    override val icon: Int = R.drawable.ic_selection_tool
    override val name: String = "Selection"
    override val description: String = "Selection tools"

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {}
}
