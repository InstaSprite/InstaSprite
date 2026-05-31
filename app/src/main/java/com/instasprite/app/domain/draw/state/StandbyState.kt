package com.instasprite.app.domain.draw.state

import com.instasprite.app.domain.tool.StrokeTool

object StandbyState : CanvasInteractionState {
    override fun onTouchStart(row: Int, col: Int, ctx: InteractionContext) {
        val tool = ctx.selectedTool
        if (tool is StrokeTool) {
            if (tool.staysPendingAfterStroke) {
                ctx.transitionTo(MovingPixelsState)
                MovingPixelsState.onTouchStart(row, col, ctx)
            } else {
                ctx.transitionTo(DrawingState)
                DrawingState.onTouchStart(row, col, ctx)
            }
        }
    }

    override fun onTouchMove(row: Int, col: Int, ctx: InteractionContext) {}
    override fun onTouchEnd(ctx: InteractionContext) {}
    override fun onTouchCancel(ctx: InteractionContext) {}
    override fun onTap(row: Int, col: Int, ctx: InteractionContext) {}
    override fun commitPending(ctx: InteractionContext) {}
    override fun cancelPending(ctx: InteractionContext) {}
}
