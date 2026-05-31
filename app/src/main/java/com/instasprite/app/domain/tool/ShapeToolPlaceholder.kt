package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

data object ShapeToolPlaceholder : ShapeTool {
    override val icon: Int = R.drawable.ic_rect_tool
    override val nameRes: Int = R.string.tool_shape
    override val descriptionRes: Int = R.string.tool_shape_desc

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
