package com.olaz.instasprite.domain.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.usecase.PixelCanvasUseCase

object FillTool : Tool {
    override val icon: Int = R.drawable.ic_fill_tool
    override val name: String = "Fill"
    override val description: String = "Fill canvas section with the selected color"

    data class FillResult(
        val dirtyMinRow: Int,
        val dirtyMinCol: Int,
        val dirtyMaxRow: Int,
        val dirtyMaxCol: Int,
    )

    var dirtyMinRow: Int = 0; private set
    var dirtyMinCol: Int = 0; private set
    var dirtyMaxRow: Int = 0; private set
    var dirtyMaxCol: Int = 0; private set

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        val result = fillDirect(canvas, row, col, color) ?: return
        dirtyMinRow = result.dirtyMinRow
        dirtyMinCol = result.dirtyMinCol
        dirtyMaxRow = result.dirtyMaxRow
        dirtyMaxCol = result.dirtyMaxCol
    }

    fun fillDirect(
        canvas: PixelCanvasUseCase,
        startRow: Int,
        startCol: Int,
        color: Color,
    ): FillResult? {
        val width = canvas.getCanvasWidth()
        val height = canvas.getCanvasHeight()

        if (startRow !in 0 until height || startCol !in 0 until width) return null

        val pixels = canvas.getActiveLayerPixelsDirect() ?: return null

        val targetArgb = pixels[startRow * width + startCol]
        val fillArgb = color.toArgb()

        if (targetArgb == fillArgb) return null

        var minRow = startRow
        var maxRow = startRow
        var minCol = startCol
        var maxCol = startCol

        // Flat visited array (row-major)
        val visited = BooleanArray(width * height)

        //   high 32 bits = row, low 32 bits = col
        val stack = ArrayDeque<Long>(256)

        fun push(r: Int, c: Int) {
            stack.addLast((r.toLong() shl 32) or (c.toLong() and 0xFFFFFFFFL))
        }
        fun pop(): Long = stack.removeLast()

        push(startRow, startCol)

        while (stack.isNotEmpty()) {
            val seed = pop()
            val seedRow = (seed ushr 32).toInt()
            val seedCol = (seed and 0xFFFFFFFFL).toInt()

            if (visited[seedRow * width + seedCol]) continue

            var left = seedCol
            while (left > 0 &&
                pixels[seedRow * width + (left - 1)] == targetArgb &&
                !visited[seedRow * width + (left - 1)]
            ) {
                left--
            }

            var right = seedCol
            while (right < width - 1 &&
                pixels[seedRow * width + (right + 1)] == targetArgb &&
                !visited[seedRow * width + (right + 1)]
            ) {
                right++
            }

            if (seedRow < minRow) minRow = seedRow
            if (seedRow > maxRow) maxRow = seedRow
            if (left   < minCol) minCol = left
            if (right  > maxCol) maxCol = right

            var inRunAbove = false
            var inRunBelow = false

            for (c in left..right) {
                val idx = seedRow * width + c
                pixels[idx] = fillArgb
                visited[idx] = true

                if (seedRow > 0) {
                    val idxAbove = (seedRow - 1) * width + c
                    if (pixels[idxAbove] == targetArgb && !visited[idxAbove]) {
                        if (!inRunAbove) {
                            push(seedRow - 1, c)
                            inRunAbove = true
                        }
                    } else {
                        inRunAbove = false
                    }
                }

                if (seedRow < height - 1) {
                    val idxBelow = (seedRow + 1) * width + c
                    if (pixels[idxBelow] == targetArgb && !visited[idxBelow]) {
                        if (!inRunBelow) {
                            push(seedRow + 1, c)
                            inRunBelow = true
                        }
                    } else {
                        inRunBelow = false
                    }
                }
            }
        }

        // Persist back into the active layer since the repository now returns an inflated buffer.
        canvas.setAllPixels(pixels)

        return FillResult(
            dirtyMinRow = minRow,
            dirtyMinCol = minCol,
            dirtyMaxRow = maxRow,
            dirtyMaxCol = maxCol,
        )
    }
}