package com.instasprite.app.ui.drawing.tutorial

import com.instasprite.app.ui.tutorial.TutorialStep

import com.instasprite.app.R

enum class DrawingTutorialStep(
    override val titleRes: Int,
    override val descriptionRes: Int
) : TutorialStep {
    CANVAS(R.string.tut_canvas_title, R.string.tut_canvas_desc),
    COLOR_PALETTE(R.string.tut_color_palette_title, R.string.tut_color_palette_desc),
    COLOR_WHEEL(R.string.tut_color_wheel_title, R.string.tut_color_wheel_desc),
    CANVAS_MENU(R.string.tut_canvas_menu_title, R.string.tut_canvas_menu_desc),
    TOOL_BUTTON(R.string.tut_tool_button_title, R.string.tut_tool_button_desc),
    TOOL_OPTION(R.string.tut_tool_option_title, R.string.tut_tool_option_desc),
    UNDO_REDO(R.string.tut_undo_redo_title, R.string.tut_undo_redo_desc),
    PROJECT_MENU(R.string.tut_project_menu_title, R.string.tut_project_menu_desc),
    LAYER_TOGGLE(R.string.tut_layer_toggle_title, R.string.tut_layer_toggle_desc)
}

val drawingTutorialSequence = listOf(
    DrawingTutorialStep.CANVAS,
    DrawingTutorialStep.COLOR_PALETTE,
    DrawingTutorialStep.COLOR_WHEEL,
    DrawingTutorialStep.CANVAS_MENU,
    DrawingTutorialStep.TOOL_BUTTON,
    DrawingTutorialStep.TOOL_OPTION,
    DrawingTutorialStep.LAYER_TOGGLE,
    DrawingTutorialStep.UNDO_REDO,
    DrawingTutorialStep.PROJECT_MENU,
)
