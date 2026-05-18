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

private class DrawingGestureHandler(
    private val canvasWidth: Int,
    private val canvasHeight: Int,
    private val selectedTool: Tool?,
    private val scale: Float,
    private val touchSlop: Float,
    private val size: IntSize,
    private val onEvent: (PixelCanvasEvent) -> Unit,
    private val onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    startEvent: PointerInputChange
) {
    private var state = DrawingGestureState.DragOrTap
    private var passedSlop = false
    private var lastCell: IntOffset? = null

    private var transformLocked = false
    private var isPanningOnly = false
    private var panAccumulated = Offset.Zero
    private var zoomAccumulated = 1f

    private val startPosition = startEvent.position
    private var pointerId = startEvent.id

    fun processEvent(event: PointerEvent): Boolean {
        val isCanceled = event.changes.any { it.isConsumed }
        if (isCanceled) {
            cancelDrawing()
            state = DrawingGestureState.Canceled
            return true
        }

        val pressedChanges = event.changes.filter { it.pressed }
        val pointerCount = pressedChanges.size

        if (pointerCount > 1 && state != DrawingGestureState.Transforming) {
            cancelDrawing()
            state = DrawingGestureState.Transforming
        }

        when (state) {
            DrawingGestureState.DragOrTap, DrawingGestureState.Drawing -> {
                handleDragOrTap(event, pressedChanges)
            }
            DrawingGestureState.Transforming -> {
                handleTransforming(event, pressedChanges, pointerCount)
            }
            DrawingGestureState.Canceled -> {}
        }
        return false
    }

    private fun cancelDrawing() {
        if (state == DrawingGestureState.Drawing) {
            onEvent(PixelCanvasEvent.OnStrokeCancel)
        }
    }

    private fun handleDragOrTap(event: PointerEvent, pressedChanges: List<PointerInputChange>) {
        val pointerChange = event.changes.firstOrNull { it.id == pointerId }
            ?: pressedChanges.firstOrNull() ?: return

        pointerId = pointerChange.id
        val distance = (pointerChange.position - startPosition).getDistance()

        if (selectedTool is StrokeTool) {
            if (state == DrawingGestureState.DragOrTap) {
                state = DrawingGestureState.Drawing
                passedSlop = true
                val startCell = startPosition.toGridCell(size.width, size.height, canvasWidth, canvasHeight)
                onEvent(PixelCanvasEvent.OnStrokeStart(startCell.x, startCell.y, scale))
                lastCell = startCell
            }

            val dragCell = pointerChange.position.toGridCell(size.width, size.height, canvasWidth, canvasHeight)
            if (dragCell != lastCell) {
                onEvent(PixelCanvasEvent.OnStrokeMove(dragCell.x, dragCell.y, scale))
                lastCell = dragCell
            }
        } else {
            if (!passedSlop && distance > touchSlop) {
                passedSlop = true
            }
        }

        if (pointerChange.positionChange() != Offset.Zero) {
            pointerChange.consume()
        }
    }

    private fun handleTransforming(event: PointerEvent, pressedChanges: List<PointerInputChange>, pointerCount: Int) {
        if (pointerCount <= 1) return

        val zoomChange = event.calculateZoom()
        val panChange = event.calculatePan()
        val centroid = pressedChanges
            .map { it.position }
            .reduce { a, b -> a + b } / pointerCount.toFloat()

        if (!transformLocked) {
            zoomAccumulated *= zoomChange
            panAccumulated += panChange

            val panDistance = panAccumulated.getDistance()
            val zoomDistance = abs(zoomAccumulated - 1f)

            if (panDistance > touchSlop) {
                transformLocked = true
                isPanningOnly = true
            } else if (zoomDistance > 0.05f) {
                transformLocked = true
                isPanningOnly = false
            }
        }

        val effectiveZoom = if (transformLocked && isPanningOnly) {
            1f
        } else {
            if (abs(zoomChange - 1f) < 0.01f) 1f else zoomChange
        }

        if (effectiveZoom != 1f || panChange != Offset.Zero) {
            onTransform(centroid, panChange, effectiveZoom)
        }

        event.changes.forEach { if (it.pressed) it.consume() }
    }

    fun onGestureEnded() {
        when (state) {
            DrawingGestureState.DragOrTap -> {
                if (!passedSlop) {
                    val cell = startPosition.toGridCell(size.width, size.height, canvasWidth, canvasHeight)
                    onEvent(PixelCanvasEvent.OnTapAt(cell.x, cell.y))
                }
            }
            DrawingGestureState.Drawing -> {
                onEvent(PixelCanvasEvent.OnStrokeEnd)
            }
            DrawingGestureState.Transforming, DrawingGestureState.Canceled -> {}
        }
    }
}

private enum class DrawingGestureState {
    DragOrTap,
    Drawing,
    Transforming,
    Canceled
}

private fun Offset.toGridCell(canvasWidth: Int, canvasHeight: Int, cols: Int, rows: Int): IntOffset {
    val cellWidth = canvasWidth.toFloat() / cols.toFloat()
    val cellHeight = canvasHeight.toFloat() / rows.toFloat()

    val gridX = (x / cellWidth).toInt().coerceIn(0, cols - 1)
    val gridY = (y / cellHeight).toInt().coerceIn(0, rows - 1)
    return IntOffset(gridX, gridY)
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

    val zoomedOffset = (currentOffset - centroidFromCenter) * (newScale / currentScale) + centroidFromCenter

    val newOffset = zoomedOffset + panChange * currentScale

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

private class CursorGestureHandler(
    private val canvasWidth: Int,
    private val canvasHeight: Int,
    private var cursorX: Float,
    private var cursorY: Float,
    private val touchSlop: Float,
    private val layoutSize: IntSize,
    private val getScale: () -> Float,
    private val getOffset: () -> Offset,
    private val getViewportSize: () -> IntSize,
    private val onCursorMove: (cursorX: Float, cursorY: Float) -> Unit,
    private val onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    startEvent: PointerInputChange
) {
    private var state = CursorGestureState.Dragging
    private var pointerId = startEvent.id
    private var lastPosition = startEvent.position

    private var transformLocked = false
    private var isPanningOnly = false
    private var panAccumulated = Offset.Zero
    private var zoomAccumulated = 1f
    private var skipEdgePan = false

    private val cellWidth = layoutSize.width.toFloat() / canvasWidth
    private val cellHeight = layoutSize.height.toFloat() / canvasHeight
    private val canvasMaxX = canvasWidth.toFloat()
    private val canvasMaxY = canvasHeight.toFloat()


    fun processEvent(event: PointerEvent): Boolean {
        if (event.changes.any { it.isConsumed }) {
            state = CursorGestureState.Canceled
            return true
        }

        val pressedChanges = event.changes.filter { it.pressed }
        val pointerCount = pressedChanges.size

        when {
            pointerCount > 1 && state != CursorGestureState.Transforming -> {
                state = CursorGestureState.Transforming
            }
            pointerCount == 1 && state == CursorGestureState.Transforming -> {
                state = CursorGestureState.Dragging
                pointerId = pressedChanges.first().id
                lastPosition = pressedChanges.first().position
                skipEdgePan = true
            }
        }

        when (state) {
            CursorGestureState.Dragging -> handleDragging(event, pressedChanges)
            CursorGestureState.Transforming -> handleTransforming(event, pressedChanges, pointerCount)
            CursorGestureState.Canceled -> {}
        }
        return false
    }

    fun onGestureEnded() {

    }

    private fun handleDragging(event: PointerEvent, pressedChanges: List<PointerInputChange>) {
        val pointerChange = event.changes.firstOrNull { it.id == pointerId }
            ?: pressedChanges.firstOrNull() ?: return

        pointerId = pointerChange.id
        val delta = pointerChange.position - lastPosition
        lastPosition = pointerChange.position

        val rawX = cursorX + delta.x / cellWidth
        val rawY = cursorY + delta.y / cellHeight

        val bounds = getVisibleGridBounds()
        val clampedX = rawX.coerceIn(bounds.minX, bounds.maxX)
        val clampedY = rawY.coerceIn(bounds.minY, bounds.maxY)

        cursorX = clampedX
        cursorY = clampedY

        applyEdgePan(
            overflowX = rawX - clampedX,
            overflowY = rawY - clampedY,
            bounds = bounds
        )
        skipEdgePan = false

        onCursorMove(cursorX, cursorY)

        if (pointerChange.positionChange() != Offset.Zero) {
            pointerChange.consume()
        }
    }

    private data class GridBounds(
        val minX: Float, val maxX: Float,
        val minY: Float, val maxY: Float
    )

    private fun getVisibleGridBounds(): GridBounds {
        val scale = getScale()
        val offset = getOffset()
        val viewport = getViewportSize()

        val canvasW = layoutSize.width.toFloat()
        val canvasH = layoutSize.height.toFloat()
        val vpHalfW = viewport.width.toFloat() / 2f
        val vpHalfH = viewport.height.toFloat() / 2f

        val minLocalX = canvasW / 2f - (vpHalfW + offset.x) / scale
        val maxLocalX = canvasW / 2f + (vpHalfW - offset.x) / scale
        val minLocalY = canvasH / 2f - (vpHalfH + offset.y) / scale
        val maxLocalY = canvasH / 2f + (vpHalfH - offset.y) / scale

        return GridBounds(
            minX = (minLocalX / cellWidth).coerceIn(0f, canvasMaxX),
            maxX = (maxLocalX / cellWidth).coerceIn(0f, canvasMaxX),
            minY = (minLocalY / cellHeight).coerceIn(0f, canvasMaxY),
            maxY = (maxLocalY / cellHeight).coerceIn(0f, canvasMaxY)
        )
    }

    private fun applyEdgePan(overflowX: Float, overflowY: Float, bounds: GridBounds) {
        if (skipEdgePan || getScale() <= 1f) return
        if (overflowX == 0f && overflowY == 0f) return

        val panX = when {
            overflowX < 0f && bounds.minX > 0f       -> -overflowX * cellWidth
            overflowX > 0f && bounds.maxX < canvasMaxX -> -overflowX * cellWidth
            else -> 0f
        }
        val panY = when {
            overflowY < 0f && bounds.minY > 0f       -> -overflowY * cellHeight
            overflowY > 0f && bounds.maxY < canvasMaxY -> -overflowY * cellHeight
            else -> 0f
        }

        if (panX != 0f || panY != 0f) {
            val center = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
            onTransform(center, Offset(panX, panY), 1f)
        }
    }

    private fun handleTransforming(event: PointerEvent, pressedChanges: List<PointerInputChange>, pointerCount: Int) {
        if (pointerCount <= 1) return

        val zoomChange = event.calculateZoom()
        val panChange = event.calculatePan()
        val centroid = pressedChanges
            .map { it.position }
            .reduce { a, b -> a + b } / pointerCount.toFloat()

        if (!transformLocked) {
            zoomAccumulated *= zoomChange
            panAccumulated += panChange

            val panDistance = panAccumulated.getDistance()
            val zoomDistance = abs(zoomAccumulated - 1f)

            if (panDistance > touchSlop) {
                transformLocked = true
                isPanningOnly = true
            } else if (zoomDistance > 0.05f) {
                transformLocked = true
                isPanningOnly = false
            }
        }

        val effectiveZoom = if (transformLocked && isPanningOnly) {
            1f
        } else {
            if (abs(zoomChange - 1f) < 0.01f) 1f else zoomChange
        }

        if (effectiveZoom != 1f || panChange != Offset.Zero) {
            onTransform(centroid, panChange, effectiveZoom)
        }

        event.changes.forEach { if (it.pressed) it.consume() }
    }
}

private enum class CursorGestureState {
    Dragging,
    Transforming,
    Canceled
}

