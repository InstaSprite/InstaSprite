package com.instasprite.app.domain.tool

import com.instasprite.app.R

enum class ToolType(val icon: Int, val nameRes: Int, val descriptionRes: Int) {
    Pencil(R.drawable.ic_pencil_tool, R.string.pencil, R.string.tool_pencil_desc),
    Eraser(R.drawable.ic_eraser_tool, R.string.eraser, R.string.tool_eraser_desc),
    Move(R.drawable.ic_move_tool, R.string.move, R.string.tool_move_desc),
    Fill(R.drawable.ic_fill_tool, R.string.fill, R.string.tool_fill_desc),
    Eyedropper(R.drawable.ic_eyedropper_tool, R.string.eyedropper, R.string.tool_eyedropper_desc),
    Shape(R.drawable.ic_rect_tool, R.string.tool_shape, R.string.tool_shape_desc),
    Selection(R.drawable.ic_selection_tool, R.string.tool_selection, R.string.tool_selection_desc),
    Line(R.drawable.ic_line_tool, R.string.tool_line, R.string.tool_line_desc),
    Rectangle(R.drawable.ic_rect_tool, R.string.tool_rectangle, R.string.tool_rectangle_desc),
    Circle(R.drawable.ic_circle_tool, R.string.tool_circle, R.string.tool_circle_desc),
    Oval(R.drawable.ic_oval_tool, R.string.tool_oval, R.string.tool_oval_desc),
    Diamond(R.drawable.ic_diamond_tool, R.string.tool_diamond, R.string.tool_diamond_desc),
    RectangleSelection(R.drawable.ic_selection_tool, R.string.tool_rectangle_selection, R.string.tool_rectangle_selection_desc),
    LassoSelection(R.drawable.ic_lasso_tool, R.string.tool_lasso_selection, R.string.tool_lasso_selection_desc),
    MagicWand(R.drawable.ic_wand_tool, R.string.tool_magic_wand, R.string.tool_magic_wand_desc)
}
