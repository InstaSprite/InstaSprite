package com.instasprite.app.domain.draw.state

import com.instasprite.app.domain.tool.StrokeTool

object MovingPixelsState : CanvasInteractionState {
    override fun onTouchStart(row: Int, col: Int, ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        ctx.historyManager.saveState()
        
        ctx.strokeEngine.onStrokeStart(
            tool = tool,
            row = row,
            col = col,
            color = ctx.activeColor,
            scale = ctx.toolSize,
            brushShape = ctx.brushShape,
            selectionState = ctx.mutableCanvasState.value.selectionState,
            zoomScale = ctx.zoomScale
        )
    }

    override fun onTouchMove(row: Int, col: Int, ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        ctx.strokeEngine.onStrokeMove(tool, row, col)
    }

    override fun onTouchEnd(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        
        ctx.strokeEngine.onStrokeEnd(
            tool = tool,
            isAppendSelectionMode = ctx.isAppendSelectionMode,
            currentSelectionState = ctx.mutableCanvasState.value.selectionState
        )
    }

    override fun onTouchCancel(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        ctx.strokeEngine.onStrokeCancel(tool)
        ctx.historyManager.restorePendingHistoryCapture()
        ctx.transitionTo(StandbyState)
    }

    override fun onTap(row: Int, col: Int, ctx: InteractionContext) {}

    override fun commitPending(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        if (ctx.strokeEngine.commitPendingTool(tool)) {
            ctx.historyManager.updateHistoryCurrentState()
        }
        ctx.transitionTo(StandbyState)
    }

    override fun cancelPending(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        if (ctx.strokeEngine.cancelPendingTool(tool)) {
            ctx.historyManager.discardHistoryCapture()
        }
        ctx.transitionTo(StandbyState)
    }
}
