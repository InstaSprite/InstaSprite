package com.instasprite.app.domain.tool.selection

import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.StrokeUpdate
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.bresenhamLine

object LassoSelectionTool : StrokeTool, SelectionTool {
    override val icon: Int = R.drawable.ic_rect_tool
    override val name: String = "Lasso"
    override val description: String = "Draw a freeform selection area"
    override val commitsImmediately: Boolean = false

    override var currentSelection: SelectionState? = null

    private val previewColor = 0xFFFFFFFF.toInt()

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private val points = mutableListOf<Pair<Int, Int>>()
    private var lastPreviewRow = -1
    private var lastPreviewCol = -1

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {}
    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color, scale: Int) {}

    override fun beginStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        color: Color,
        scale: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        canvasWidth = canvas.getCanvasWidth()
        canvasHeight = canvas.getCanvasHeight()

        points.clear()
        
        // Add start point
        if (row in 0 until canvasHeight && col in 0 until canvasWidth) {
            points.add(row to col)
            plotPreviewPixel(row, col, previewColor)
        }
        
        lastPreviewRow = row
        lastPreviewCol = col

        return StrokeUpdate(isFullPreview = false)
    }

    override fun updateStroke(
        canvas: PixelCanvasUseCase,
        row: Int,
        col: Int,
        plotPreviewPixel: (row: Int, col: Int, color: Int) -> Unit,
        onCommittedPixel: (row: Int, col: Int) -> Unit
    ): StrokeUpdate {
        if (lastPreviewRow != -1 && lastPreviewCol != -1) {
            val linePoints = bresenhamLine(lastPreviewCol, lastPreviewRow, col, row)
            for (i in 1 until linePoints.size) {
                val pt = linePoints[i]
                if (pt.second in 0 until canvasHeight && pt.first in 0 until canvasWidth) {
                    points.add(pt.second to pt.first)
                }
            }
        }
        
        lastPreviewRow = row
        lastPreviewCol = col

        // Redraw the entire path so far
        for (pt in points) {
            plotPreviewPixel(pt.first, pt.second, previewColor)
        }

        // Draw the auto-closing preview line back to start
        if (points.isNotEmpty()) {
            val firstPt = points.first()
            val closeLine = bresenhamLine(col, row, firstPt.second, firstPt.first)
            for (pt in closeLine) {
                plotPreviewPixel(pt.second, pt.first, previewColor)
            }
        }

        return StrokeUpdate(isFullPreview = true)
    }

    override fun endStroke() {
        if (points.isEmpty()) {
            currentSelection = null
            return
        }

        // 1. Draw boundary directly into mask
        val mask = BooleanArray(canvasWidth * canvasHeight)
        
        var minRow = canvasHeight
        var maxRow = -1
        var minCol = canvasWidth
        var maxCol = -1

        val allBoundaryPoints = points.toMutableList()
        val firstPt = points.first()
        val lastPt = points.last()
        val closeLine = bresenhamLine(lastPt.second, lastPt.first, firstPt.second, firstPt.first)
        for (pt in closeLine) {
            allBoundaryPoints.add(pt.second to pt.first) // Convert Pair(col, row) to Pair(row, col)
        }

        for (pt in allBoundaryPoints) {
            val r = pt.first
            val c = pt.second
            if (r in 0 until canvasHeight && c in 0 until canvasWidth) {
                mask[r * canvasWidth + c] = true
                if (r < minRow) minRow = r
                if (r > maxRow) maxRow = r
                if (c < minCol) minCol = c
                if (c > maxCol) maxCol = c
            }
        }

        // Pad bounding box by 1 to allow exterior flood fill to flow around the shape
        val padMinRow = maxOf(0, minRow - 1)
        val padMaxRow = minOf(canvasHeight - 1, maxRow + 1)
        val padMinCol = maxOf(0, minCol - 1)
        val padMaxCol = minOf(canvasWidth - 1, maxCol + 1)

        val exterior = BooleanArray(canvasWidth * canvasHeight)
        val queue = java.util.ArrayDeque<Int>()

        // Add padded bounding box perimeter to flood fill queue
        for (r in padMinRow..padMaxRow) {
            for (c in padMinCol..padMaxCol) {
                if (r == padMinRow || r == padMaxRow || c == padMinCol || c == padMaxCol) {
                    val idx = r * canvasWidth + c
                    if (!mask[idx]) {
                        exterior[idx] = true
                        queue.add(idx)
                    }
                }
            }
        }

        val dx = intArrayOf(-1, 1, 0, 0)
        val dy = intArrayOf(0, 0, -1, 1)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            val r = curr / canvasWidth
            val c = curr % canvasWidth

            for (i in 0..3) {
                val nr = r + dy[i]
                val nc = c + dx[i]
                if (nr in padMinRow..padMaxRow && nc in padMinCol..padMaxCol) {
                    val nIdx = nr * canvasWidth + nc
                    if (!mask[nIdx] && !exterior[nIdx]) {
                        exterior[nIdx] = true
                        queue.addLast(nIdx)
                    }
                }
            }
        }

        var hasSelection = false
        for (r in padMinRow..padMaxRow) {
            for (c in padMinCol..padMaxCol) {
                val idx = r * canvasWidth + c
                if (!exterior[idx]) {
                    mask[idx] = true
                    hasSelection = true
                }
            }
        }

        if (hasSelection) {
            currentSelection = SelectionState(
                mask = mask,
                bounds = Rect(minCol, minRow, maxCol + 1, maxRow + 1),
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        } else {
            currentSelection = null
        }

        points.clear()
        lastPreviewRow = -1
        lastPreviewCol = -1
    }

    override fun cancelStroke() {
        points.clear()
        lastPreviewRow = -1
        lastPreviewCol = -1
    }

}
