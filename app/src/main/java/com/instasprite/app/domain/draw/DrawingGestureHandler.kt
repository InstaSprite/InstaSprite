package com.instasprite.app.domain.draw

import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.drawing.contract.PixelCanvasEvent
import kotlin.math.abs

internal enum class DrawingGestureState {
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

internal class DrawingGestureHandler(
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
