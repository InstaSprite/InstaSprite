package com.olaz.instasprite.utils

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.olaz.instasprite.domain.draw.DrawUtils
import com.olaz.instasprite.domain.tool.EraserTool
import com.olaz.instasprite.domain.tool.FillTool
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import kotlin.collections.contains


private fun DrawScope.drawGridOverlay(
    canvasWidth: Int,
    canvasHeight: Int,
    gridColor: Color,
    gridStroke: Stroke
) {
    val cellWidth = size.width / canvasWidth
    val cellHeight = size.height / canvasHeight
    val cellSize = Size(cellWidth, cellHeight)

    for (row in 0 until canvasHeight) {
        val y = row * cellHeight
        for (col in 0 until canvasWidth) {
            val x = col * cellWidth
            val topLeft = Offset(x, y)

            drawRect(
                color = gridColor,
                topLeft = topLeft,
                size = cellSize,
                style = gridStroke
            )
        }
    }
}

fun Offset.toGridCell(canvasWidth: Int, canvasHeight: Int, cols: Int, rows: Int): IntOffset {
    val cellWidth = canvasWidth.toFloat() / cols.toFloat()
    val cellHeight = canvasHeight.toFloat() / rows.toFloat()

    val gridX = (x / cellWidth).toInt().coerceIn(0, cols - 1)
    val gridY = (y / cellHeight).toInt().coerceIn(0, rows - 1)
    return IntOffset(gridX, gridY)
}

@Composable
fun Modifier.drawingPointerInput(
    canvasWidth: Int,
    canvasHeight: Int,
    selectedTool: Tool?,
    onEvent: (PixelCanvasEvent) -> Unit
): Modifier {
    if (canvasWidth == 0 || canvasHeight == 0) return this

    return this.pointerInput(canvasWidth, canvasHeight, selectedTool) {
        awaitEachGesture {
            if (selectedTool in listOf(PencilTool, EraserTool, FillTool)) {
                onEvent(PixelCanvasEvent.OnCanvasTouchStart)
            }

            val down = awaitFirstDown(requireUnconsumed = false)
            val event = awaitPointerEvent()
            val pointerCount = event.changes.count { it.pressed }

            if (pointerCount > 1) {
                return@awaitEachGesture
            }

            val startCell = down.position.toGridCell(
                size.width, size.height,
                canvasWidth, canvasHeight
            )

            onEvent(PixelCanvasEvent.DrawAt(startCell.x, startCell.y))

            if (selectedTool in listOf(PencilTool, EraserTool)) {
                var lastCell = startCell

                drag(down.id) { change ->
                    change.consume()
                    val dragCell = change.position.toGridCell(
                        size.width, size.height,
                        canvasWidth, canvasHeight
                    )

                    if (dragCell != lastCell) {
                        val linePoints = DrawUtils.bresenhamLine(
                            lastCell.x.toInt(), lastCell.y.toInt(),
                            dragCell.x.toInt(), dragCell.y.toInt()
                        )

                        for ((x, y) in linePoints) {
                            onEvent(PixelCanvasEvent.DrawAt(x, y))
                        }

                        lastCell = dragCell
                    }
                }
            }
        }
    }
}

fun updateBitmapPixels(
    bitmap: Bitmap,
    pixels: List<Color>
) {
    val width = bitmap.width
    val height = bitmap.height
    val pixelArray = IntArray(width * height)

    for (i in pixels.indices) {
        pixelArray[i] = pixels[i].toArgb()
    }

    bitmap.setPixels(pixelArray, 0, width, 0, 0, width, height)
}

fun Modifier.drawCheckerboard(
    canvasWidth: Int,
    canvasHeight: Int,
    checkerColor1: Color = CatppuccinUI.CanvasChecker1Color,
    checkerColor2: Color = CatppuccinUI.CanvasChecker2Color
): Modifier = this.drawBehind {
    if (canvasWidth == 0 || canvasHeight == 0) return@drawBehind

    drawRect(color = checkerColor2)
    
    val cellWidth = size.width / canvasWidth
    val cellHeight = size.height / canvasHeight

    val useLargeCheckers = canvasWidth >= 32 || canvasHeight >= 32
    val blockSize = if (useLargeCheckers) 16 else 1

    val numCols = canvasWidth / blockSize
    val numRows = canvasHeight / blockSize

    val blockWidth = cellWidth * blockSize
    val blockHeight = cellHeight * blockSize

    for (row in 0 until numRows) {
        val y = row * blockHeight
        for (col in 0 until numCols) {
            if ((row + col) % 2 == 0) {
                drawRect(
                    color = checkerColor1,
                    topLeft = Offset(col * blockWidth, y),
                    size = Size(blockWidth, blockHeight)
                )
            }
        }
    }
}
