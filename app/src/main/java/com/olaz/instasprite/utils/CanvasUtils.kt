package com.olaz.instasprite.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.olaz.instasprite.ui.theme.CatppuccinUI


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