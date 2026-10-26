package com.instasprite.app.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

fun Rect.anchoredTo(viewport: Rect): Rect = Rect(
    left.coerceIn(viewport.left, viewport.right), top.coerceIn(viewport.top, viewport.bottom),
    right.coerceIn(viewport.left, viewport.right), bottom.coerceIn(viewport.top, viewport.bottom)
)

fun visibleCanvasBounds(layout: IntSize, viewport: IntSize, scale: Float, offset: Offset): Rect {
    if (layout.width <= 0 || layout.height <= 0 || viewport.width <= 0 || viewport.height <= 0 || scale <= 0) return Rect.Zero
    val left = (layout.width / 2f - (viewport.width / 2f + offset.x) / scale).coerceIn(
        0f,
        layout.width.toFloat()
    )
    val right = (layout.width / 2f + (viewport.width / 2f - offset.x) / scale).coerceIn(
        0f,
        layout.width.toFloat()
    )
    val top = (layout.height / 2f - (viewport.height / 2f + offset.y) / scale).coerceIn(
        0f,
        layout.height.toFloat()
    )
    val bottom = (layout.height / 2f + (viewport.height / 2f - offset.y) / scale).coerceIn(
        0f,
        layout.height.toFloat()
    )
    return Rect(left, top, right, bottom)
}
