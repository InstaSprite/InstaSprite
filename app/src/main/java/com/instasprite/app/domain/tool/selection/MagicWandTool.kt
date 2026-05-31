package com.instasprite.app.domain.tool.selection

import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import com.instasprite.app.R
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import java.util.ArrayDeque

object MagicWandTool : Tool, SelectionTool {
    override val icon: Int = R.drawable.ic_wand_tool
    override val nameRes: Int = R.string.tool_magic_wand
    override val descriptionRes: Int = R.string.tool_magic_wand_desc

    override var currentSelection: SelectionState? = null

    override fun apply(canvas: PixelCanvasUseCase, row: Int, col: Int, color: Color) {
        val width = canvas.getCanvasWidth()
        val height = canvas.getCanvasHeight()
        
        if (row !in 0 until height || col !in 0 until width) {
            currentSelection = null
            return
        }

        val pixels = canvas.getActiveLayerPixelsDirect() ?: run {
            currentSelection = null
            return
        }
        val targetColor = pixels[row * width + col]
        
        val mask = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()
        
        val startIndex = row * width + col
        queue.add(startIndex)
        mask[startIndex] = true

        var minRow = row
        var maxRow = row
        var minCol = col
        var maxCol = col

        val dx = intArrayOf(-1, 1, 0, 0)
        val dy = intArrayOf(0, 0, -1, 1)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            val r = curr / width
            val c = curr % width

            if (r < minRow) minRow = r
            if (r > maxRow) maxRow = r
            if (c < minCol) minCol = c
            if (c > maxCol) maxCol = c

            for (i in 0..3) {
                val nr = r + dy[i]
                val nc = c + dx[i]
                
                if (nr in 0 until height && nc in 0 until width) {
                    val nIndex = nr * width + nc
                    if (!mask[nIndex] && pixels[nIndex] == targetColor) {
                        mask[nIndex] = true
                        queue.addLast(nIndex)
                    }
                }
            }
        }

        currentSelection = SelectionState(
            mask = mask,
            bounds = Rect(minCol, minRow, maxCol + 1, maxRow + 1),
            canvasWidth = width,
            canvasHeight = height
        )
    }
}
