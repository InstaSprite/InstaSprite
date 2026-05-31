package com.instasprite.app.domain.tool.selection

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

object SelectionToolPlaceholder : Tool {
    override val icon: Int = R.drawable.ic_selection_tool
    override val nameRes: Int = R.string.tool_selection
    override val descriptionRes: Int = R.string.tool_selection_desc

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {}
}
