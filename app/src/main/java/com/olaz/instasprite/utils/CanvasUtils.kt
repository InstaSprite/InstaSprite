package com.olaz.instasprite.utils

import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.model.SelectionState
import com.olaz.instasprite.ui.theme.CatppuccinUI
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.olaz.instasprite.domain.tool.EraserTool
import com.olaz.instasprite.domain.tool.EyedropperTool
import com.olaz.instasprite.domain.tool.FillTool
import com.olaz.instasprite.domain.tool.MoveTool
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.ShapeTool
import com.olaz.instasprite.domain.tool.StrokeTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.tool.selection.SelectionTool
import com.olaz.instasprite.ui.drawing.contract.CursorState


fun DrawScope.drawCursorOverlay(
    cursorState: CursorState,
    selectedTool: Tool?,
    toolSize: Int,
    canvasWidth: Int,
    canvasHeight: Int,
    dstSize: IntSize,
    activeColor: Color,
    scale: Float,
    toolIconBitmap: ImageBitmap? = null
) {
    if (!cursorState.isVisible) return

    val pxW = dstSize.width.toFloat() / canvasWidth
    val pxH = dstSize.height.toFloat() / canvasHeight

    val cursorScreenX = cursorState.cursorX * pxW
    val cursorScreenY = cursorState.cursorY * pxH

    val gridCellX = cursorState.gridX
    val gridCellY = cursorState.gridY

    val cursorSizeCells = when (selectedTool) {
        is StrokeTool -> toolSize
        else -> 1
    }

    val halfExpand = (cursorSizeCells - 1) / 2
    val startCol = (gridCellX - halfExpand).coerceAtLeast(0)
    val startRow = (gridCellY - halfExpand).coerceAtLeast(0)
    val endCol = (gridCellX + (cursorSizeCells - halfExpand - 1)).coerceAtMost(canvasWidth - 1)
    val endRow = (gridCellY + (cursorSizeCells - halfExpand - 1)).coerceAtMost(canvasHeight - 1)

    val cellLeft = startCol * pxW
    val cellTop = startRow * pxH
    val cellRight = (endCol + 1) * pxW
    val cellBottom = (endRow + 1) * pxH

//    val fillColor = when (selectedTool) {
//        is EraserTool -> Color.White.copy(alpha = 0.25f)
//        is PencilTool -> activeColor.copy(alpha = 0.35f)
//        else -> Color.White.copy(alpha = 0.15f)
//    }
//
//    drawRect(
//        color = fillColor,
//        topLeft = Offset(cellLeft, cellTop),
//        size = Size(cellRight - cellLeft, cellBottom - cellTop)
//    )

    val strokeWidth = 1.5f.dp.toPx() / scale

    drawRect(
        color = Color.White,
        topLeft = Offset(cellLeft + strokeWidth, cellTop + strokeWidth),
        size = Size(cellRight - cellLeft - strokeWidth * 2, cellBottom - cellTop - strokeWidth * 2),
        style = Stroke(width = strokeWidth),
    )

    drawRect(
        color = Color.Black,
        topLeft = Offset(cellLeft, cellTop),
        size = Size(cellRight - cellLeft, cellBottom - cellTop),
        style = Stroke(width = strokeWidth),
    )

    val iconSizePx = 32.dp.toPx() / scale

    if (toolIconBitmap != null) {
        val iconX = cursorScreenX
        val iconY = cursorScreenY - iconSizePx

        val scaleX = iconSizePx / toolIconBitmap.width
        val scaleY = iconSizePx / toolIconBitmap.height

        translate(left = iconX, top = iconY) {
            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
                drawImage(image = toolIconBitmap)
            }
        }
    } else { // crosshair
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
    checkerColor1: Color = CatppuccinUI.CanvasChecker1Color,
    checkerColor2: Color = CatppuccinUI.CanvasChecker2Color
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