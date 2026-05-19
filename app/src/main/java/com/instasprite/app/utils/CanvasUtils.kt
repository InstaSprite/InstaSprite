package com.instasprite.app.utils

import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.BrushStamp
import com.instasprite.app.domain.tool.EraserTool
import com.instasprite.app.domain.tool.PencilTool
import com.instasprite.app.domain.tool.ShapeTool
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.drawing.contract.CursorState


fun DrawScope.drawCursorOverlay(
    cursorState: CursorState,
    selectedTool: Tool?,
    toolSize: Int,
    brushShape: BrushShape = BrushShape.Square,
    canvasWidth: Int,
    canvasHeight: Int,
    dstSize: IntSize,
    scale: Float,
    toolIconBitmap: ImageBitmap? = null,
    cursorIconBitmap: ImageBitmap? = null,
    cursorColor: Color? = null,
    useToolIcon: Boolean = false
) {
    if (!cursorState.isVisible) return

    val pxW = dstSize.width.toFloat() / canvasWidth
    val pxH = dstSize.height.toFloat() / canvasHeight

    val cursorScreenX = cursorState.cursorX * pxW
    val cursorScreenY = cursorState.cursorY * pxH

    val gridCellX = cursorState.gridX
    val gridCellY = cursorState.gridY

    val cursorSizeCells = when (selectedTool) {
        is PencilTool, is EraserTool, is ShapeTool -> toolSize
        else -> 1
    }

    val stamp = BrushStamp.create(brushShape, cursorSizeCells)
    val outlinePath = stamp.createPath(pxW, pxH)

    val strokeWidth = 1.5f.dp.toPx() / scale

    translate(left = gridCellX * pxW, top = gridCellY * pxH) {
        drawPath(
            path = outlinePath,
            color = Color.Black,
            style = Stroke(width = strokeWidth * 3)
        )
        drawPath(
            path = outlinePath,
            color = Color.White,
            style = Stroke(width = strokeWidth)
        )
    }

    val iconSizePx = 32.dp.toPx() / scale

    if (toolIconBitmap != null && cursorIconBitmap != null) {
        if (!useToolIcon) {
            val iconX = cursorScreenX
            val iconY = cursorScreenY

            val scaleX = iconSizePx / cursorIconBitmap.width
            val scaleY = iconSizePx / cursorIconBitmap.height
            translate(left = iconX, top = iconY) {
                scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                    drawImage(image = cursorIconBitmap, filterQuality = FilterQuality.None)
                    drawColorPreview(
                        cursorColor, toolIconBitmap, Offset(
                            // tool icon dimension = 14x14
                            toolIconBitmap.width * 0.5f,
                            toolIconBitmap.width * 0.5f
                        )
                    )
                }
            }
        } else {
            val iconX = cursorScreenX
            val iconY = cursorScreenY - iconSizePx

            val scaleX = iconSizePx / toolIconBitmap.width
            val scaleY = iconSizePx / toolIconBitmap.height

            translate(left = iconX, top = iconY) {
                scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                    drawImage(image = toolIconBitmap, filterQuality = FilterQuality.None)
                    drawColorPreview(
                        cursorColor, toolIconBitmap, Offset(
                            8f,
                            -2.5f
                        )
                    )
                }
            }

        }
    } else { // crosshair
        drawCrosshair(cursorScreenX, cursorScreenY, iconSizePx, scale)
    }
}

private fun DrawScope.drawCrosshair(
    cursorScreenX: Float,
    cursorScreenY: Float,
    iconSizePx: Float,
    scale: Float
) {
    val centerX = cursorScreenX
    val centerY = cursorScreenY

    val crosshairSize = iconSizePx * 0.4f

    val strokeOuter = 5.dp.toPx() / scale
    val strokeInner = 3.dp.toPx() / scale

    drawLine(
        color = Color.Black,
        start = Offset(centerX - crosshairSize, centerY),
        end = Offset(centerX + crosshairSize, centerY),
        strokeWidth = strokeOuter,
        cap = StrokeCap.Square
    )

    drawLine(
        color = Color.Black,
        start = Offset(centerX, centerY - crosshairSize),
        end = Offset(centerX, centerY + crosshairSize),
        strokeWidth = strokeOuter,
        cap = StrokeCap.Square
    )

    drawLine(
        color = Color.White,
        start = Offset(centerX - crosshairSize, centerY),
        end = Offset(centerX + crosshairSize, centerY),
        strokeWidth = strokeInner,
        cap = StrokeCap.Square
    )

    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - crosshairSize),
        end = Offset(centerX, centerY + crosshairSize),
        strokeWidth = strokeInner,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawColorPreview(
    cursorColor: Color?,
    toolIconBitmap: ImageBitmap,
    offset: Offset
) {
    if (cursorColor != null && cursorColor != Color.Transparent) {
        val indicatorSize = 9f
        val stroke = 0.7f

        val topLeft = offset

        drawRect(
            color = Color.Black,
            topLeft = topLeft,
            size = Size(indicatorSize, indicatorSize)
        )

        drawRect(
            color = Color.White,
            topLeft = topLeft + Offset(stroke, stroke),
            size = Size(indicatorSize - stroke * 2, indicatorSize - stroke * 2)
        )

        drawRect(
            color = cursorColor,
            topLeft = topLeft + Offset(stroke * 2, stroke * 2),
            size = Size(indicatorSize - stroke * 4, indicatorSize - stroke * 4)
        )
    }
}


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
    checkerColor1: Color = Color(0xFF808080),
    checkerColor2: Color = Color(0xFFC0C0C0)
): Modifier = this.drawWithCache {
    if (canvasWidth == 0 || canvasHeight == 0) {
        return@drawWithCache onDrawBehind {}
    }

    val cellWidth = size.width / canvasWidth
    val cellHeight = size.height / canvasHeight

    val useLargeCheckers = canvasWidth >= 32 || canvasHeight >= 32
    val blockSize = if (useLargeCheckers) 16 else 1

    val blockWidth = cellWidth * blockSize
    val blockHeight = cellHeight * blockSize

    val bitmap = createBitmap(2, 2)
    val c1 = checkerColor1.toArgb()
    val c2 = checkerColor2.toArgb()
    bitmap[0, 0] = c1
    bitmap[1, 1] = c1
    bitmap[1, 0] = c2
    bitmap[0, 1] = c2

    val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    val matrix = Matrix()
    matrix.setScale(blockWidth, blockHeight)
    shader.setLocalMatrix(matrix)

    val composePaint = Paint().apply {
        this.shader = shader
        this.filterQuality = FilterQuality.None
        this.isAntiAlias = false
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.drawRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                paint = composePaint
            )
        }
    }
}

fun Modifier.drawCheckerboard(
    cellSizePx: Float = 8f,
    checkerColor1: Color = Color(0xFF808080),
    checkerColor2: Color = Color(0xFFC0C0C0)
): Modifier = this.drawWithCache {
    val bitmap = createBitmap(2, 2)
    val c1 = checkerColor1.toArgb()
    val c2 = checkerColor2.toArgb()
    bitmap[0, 0] = c1
    bitmap[1, 1] = c1
    bitmap[1, 0] = c2
    bitmap[0, 1] = c2

    val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    val matrix = Matrix()
    matrix.setScale(cellSizePx, cellSizePx)
    shader.setLocalMatrix(matrix)

    val composePaint = Paint().apply {
        this.shader = shader
        this.filterQuality = FilterQuality.None
        this.isAntiAlias = false
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.drawRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                paint = composePaint
            )
        }
    }
}