package com.instasprite.app.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.drawing.contract.CursorState
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import com.instasprite.app.domain.draw.CursorGestureHandler
import com.instasprite.app.domain.draw.DrawingGestureHandler
import kotlin.math.abs



fun bresenhamLine(x0: Int, y0: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
    val points = mutableListOf<Pair<Int, Int>>()
    val dx = abs(x1 - x0)
    val dy = -abs(y1 - y0)
    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1
    var err = dx + dy
    var x = x0
    var y = y0

    while (true) {
        points.add(x to y)
        if (x == x1 && y == y1) break
        val e2 = 2 * err
        if (e2 >= dy) {
            err += dy
            x += sx
        }
        if (e2 <= dx) {
            err += dx
            y += sy
        }
    }
    return points
}

inline fun bresenhamLine(x0: Int, y0: Int, x1: Int, y1: Int, action: (x: Int, y: Int) -> Unit) {
    val dx = abs(x1 - x0)
    val dy = -abs(y1 - y0)
    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1
    var err = dx + dy
    var x = x0
    var y = y0

    while (true) {
        action(x, y)
        if (x == x1 && y == y1) break
        val e2 = 2 * err
        if (e2 >= dy) {
            err += dy
            x += sx
        }
        if (e2 <= dx) {
            err += dx
            y += sy
        }
    }
}

@Composable
fun Modifier.drawingPointerInput(
    canvasWidth: Int,
    canvasHeight: Int,
    selectedTool: Tool?,
    scale: Float,
    onEvent: (PixelCanvasEvent) -> Unit,
    onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
): Modifier {
    if (canvasWidth == 0 || canvasHeight == 0) return this

    val touchSlop = LocalViewConfiguration.current.touchSlop

    return this.pointerInput(canvasWidth, canvasHeight, selectedTool) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val handler = DrawingGestureHandler(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                selectedTool = selectedTool,
                scale = scale,
                touchSlop = touchSlop,
                size = size,
                onEvent = onEvent,
                onTransform = onTransform,
                startEvent = down
            )

            do {
                val event = awaitPointerEvent()
                val canceled = handler.processEvent(event)
                if (canceled) break
            } while (event.changes.any { it.pressed })

            handler.onGestureEnded()
        }
    }
}

fun calculateNewScaleAndOffset(
    centroid: Offset,
    panChange: Offset,
    zoomChange: Float,
    currentScale: Float,
    currentOffset: Offset,
    layoutSize: IntSize,
    maxScale: Float
): Pair<Float, Offset> {
    val newScale = (currentScale * zoomChange).coerceIn(1f, maxScale)

    val layoutCenter = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
    val centroidFromCenter = centroid - layoutCenter

    // The screen pan is panChange * currentScale.
    val newOffset = currentOffset + panChange * currentScale - centroidFromCenter * (newScale - currentScale)

    val extraWidth = (layoutSize.width * (newScale - 1f)) / 2f
    val extraHeight = (layoutSize.height * (newScale - 1f)) / 2f

    return Pair(
        newScale,
        Offset(
            x = newOffset.x.coerceIn(-extraWidth, extraWidth),
            y = newOffset.y.coerceIn(-extraHeight, extraHeight)
        )
    )
}

@Composable
fun Modifier.cursorPointerInput(
    canvasWidth: Int,
    canvasHeight: Int,
    cursorState: CursorState,
    scale: Float,
    offset: Offset,
    viewportSize: IntSize,
    onCursorMove: (cursorX: Float, cursorY: Float) -> Unit,
    onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
): Modifier {
    if (canvasWidth == 0 || canvasHeight == 0) return this

    val touchSlop = LocalViewConfiguration.current.touchSlop
    val latestCursorX by rememberUpdatedState(cursorState.cursorX)
    val latestCursorY by rememberUpdatedState(cursorState.cursorY)
    val latestScale by rememberUpdatedState(scale)
    val latestOffset by rememberUpdatedState(offset)
    val latestViewportSize by rememberUpdatedState(viewportSize)

    return this.pointerInput(canvasWidth, canvasHeight) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val handler = CursorGestureHandler(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                cursorX = latestCursorX,
                cursorY = latestCursorY,
                touchSlop = touchSlop,
                layoutSize = size,
                getScale = { latestScale },
                getOffset = { latestOffset },
                getViewportSize = { latestViewportSize },
                onCursorMove = onCursorMove,
                onTransform = onTransform,
                startEvent = down
            )

            do {
                val event = awaitPointerEvent()
                val canceled = handler.processEvent(event)
                if (canceled) break
            } while (event.changes.any { it.pressed })

            handler.onGestureEnded()
        }
    }
}





