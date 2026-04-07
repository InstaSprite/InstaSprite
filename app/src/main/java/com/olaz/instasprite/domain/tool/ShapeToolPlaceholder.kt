package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

data object ShapeToolPlaceholder : ShapeTool {
    override val icon: Int = R.drawable.ic_rect_tool
    override val name: String = "Shape"
    override val description: String = "Select a shape tool"

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
    }

    override val commitsImmediately: Boolean = false

    override fun beginStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        return StrokeUpdate()
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        return StrokeUpdate()
    }

    override fun endStroke() {
    }

    override fun cancelStroke() {

    }
}
