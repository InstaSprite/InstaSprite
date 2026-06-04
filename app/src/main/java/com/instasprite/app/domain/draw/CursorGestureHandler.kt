package com.instasprite.app.domain.draw

import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntSize
import com.instasprite.app.ui.drawing.contract.CursorState
import kotlin.math.abs

internal enum class CursorGestureState {
    Dragging,
    Transforming,
    Canceled
}

internal data class GridBounds(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float
)

internal class CursorGestureHandler(
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
