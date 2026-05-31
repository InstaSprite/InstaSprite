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

    override fun commitPending(ctx: InteractionContext): Boolean {
        val tool = ctx.selectedTool as? StrokeTool ?: return false
        val committed = ctx.strokeEngine.commitPendingTool(tool)
        if (committed) {
            ctx.historyManager.updateHistoryCurrentState()
            ctx.refreshLayerState()
        }
        ctx.transitionTo(StandbyState)
        return committed
    }

    override fun cancelPending(ctx: InteractionContext): Boolean {
        val tool = ctx.selectedTool as? StrokeTool ?: return false
        val cancelled = ctx.strokeEngine.cancelPendingTool(tool)
        if (cancelled) {
            ctx.historyManager.discardHistoryCapture()
            ctx.refreshLayerState()
        }
        ctx.transitionTo(StandbyState)
        return cancelled
    }
}
