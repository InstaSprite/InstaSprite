package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
internal fun TimelineScrollbar(state: LazyListState, itemWidth: Dp, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Canvas(
        modifier
            .fillMaxWidth()
            .height(2.pixelDp)
    ) {
        if (!state.canScrollBackward && !state.canScrollForward) return@Canvas
        val contentWidth = state.layoutInfo.totalItemsCount * itemWidth.toPx()
        val viewportWidth =
            (state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset).toFloat()
        if (contentWidth <= viewportWidth || size.width <= 0f) return@Canvas
        val thumbWidth = (size.width * viewportWidth / contentWidth)
            .coerceIn(8.pixelDp.toPx().coerceAtMost(size.width), size.width)
        val scrollOffset =
            state.firstVisibleItemIndex * itemWidth.toPx() + state.firstVisibleItemScrollOffset
        val x = (scrollOffset / (contentWidth - viewportWidth)).coerceIn(
            0f,
            1f
        ) * (size.width - thumbWidth)
        drawRect(colors.surface)
        drawRect(colors.surfaceHighest, Offset(x, 0f), Size(thumbWidth, size.height))
    }
}
