package com.olaz.instasprite.utils

import android.graphics.Matrix
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.model.SelectionState
import com.olaz.instasprite.ui.theme.CatppuccinUI


fun DrawScope.drawSelectionOverlay(
    selectionState: SelectionState,
    showGrabHandle: Boolean,
    canvasWidth: Int,
    canvasHeight: Int,
    dstSize: IntSize,
    scale: Float
) {
    val pxW = dstSize.width.toFloat() / canvasWidth
    val pxH = dstSize.height.toFloat() / canvasHeight

    val matrix = Matrix()
    matrix.setScale(pxW, pxH)
    val scaledPath = Path().apply {
        addPath(selectionState.outlinePath)
        asAndroidPath().transform(matrix)
    }

    val outlineWidth = 2.dp.toPx() / scale
    val bgOutlineWidth = 3.dp.toPx() / scale

    drawPath(
        path = scaledPath,
        color = Color.Black,
        style = Stroke(width = bgOutlineWidth),

    )
    drawPath(
        path = scaledPath,
        color = Color.White,
        style = Stroke(width = outlineWidth)
    )

    if (showGrabHandle) {
        val b = selectionState.bounds
        val handleSize = 16.dp.toPx() / scale
        val strokeWidth = 1.dp.toPx() / scale

        val left = b.left * pxW
        val right = b.right * pxW
        val top = b.top * pxH
        val bottom = b.bottom * pxH
        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f

        val points = listOf(
            Offset(left, top),
            Offset(right, top),
            Offset(left, bottom),
            Offset(right, bottom),
            Offset(midX, top),
            Offset(midX, bottom),
            Offset(left, midY),
            Offset(right, midY)
        )

        for (pt in points) {
            drawRect(
                color = Color.White,
                topLeft = Offset(pt.x - handleSize / 2, pt.y - handleSize / 2),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(pt.x - handleSize / 2, pt.y - handleSize / 2),
                size = Size(handleSize, handleSize),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

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