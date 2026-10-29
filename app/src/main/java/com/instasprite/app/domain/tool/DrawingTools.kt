package com.instasprite.app.domain.tool

import com.instasprite.app.domain.tool.selection.LassoSelectionTool
import com.instasprite.app.domain.tool.selection.MagicWandTool
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
import com.instasprite.app.domain.tool.selection.SelectionTool
import com.instasprite.app.domain.tool.shape.CircleTool
import com.instasprite.app.domain.tool.shape.DiamondTool
import com.instasprite.app.domain.tool.shape.LineTool
import com.instasprite.app.domain.tool.shape.OvalTool
import com.instasprite.app.domain.tool.shape.RectangleTool

/** Mutable tools belong to one editor and survive recomposition and tool switching. */
class DrawingTools {
    val pencil = PencilTool()
    val move = MoveTool()
    private val tools = listOf(
        pencil, move, EraserTool(), FillTool, EyedropperTool,
        LineTool(), RectangleTool(), CircleTool(), OvalTool(), DiamondTool(),
        RectangleSelectionTool(), LassoSelectionTool(), MagicWandTool()
    ).associateBy { it.type }

    operator fun get(type: ToolType): Tool = tools.getValue(
        when (type) {
            ToolType.Shape -> ToolType.Line
            ToolType.Selection -> ToolType.RectangleSelection
            else -> type
        }
    )

    fun reset() {
        tools.values.forEach { tool ->
            (tool as? StrokeTool)?.cancelStroke()
            (tool as? SelectionTool)?.clearSelection()
        }
    }
}
