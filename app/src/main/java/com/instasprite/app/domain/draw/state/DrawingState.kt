package com.instasprite.app.domain.draw.state

import com.instasprite.app.domain.tool.StrokeTool
import com.instasprite.app.domain.tool.selection.SelectionTool

object DrawingState : CanvasInteractionState {
    override fun onTouchStart(row: Int, col: Int, ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        if (tool !is SelectionTool) {
            ctx.historyManager.saveState()
        }
        val selectionState = ctx.mutableCanvasState.value.selectionState
        ctx.strokeEngine.onStrokeStart(
            tool = tool,
            row = row,
            col = col,
            color = ctx.activeColor,
            scale = ctx.toolSize,
            brushShape = ctx.brushShape,
            selectionState = selectionState,
            zoomScale = ctx.zoomScale
        )
    }

    override fun onTouchMove(row: Int, col: Int, ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        ctx.strokeEngine.onStrokeMove(tool, row, col)
    }

    override fun onTouchEnd(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        val currentSelectionState = ctx.mutableCanvasState.value.selectionState
        
        val result = ctx.strokeEngine.onStrokeEnd(
            tool = tool,
            isAppendSelectionMode = ctx.isAppendSelectionMode,
            currentSelectionState = currentSelectionState
        )

        if (result.updatedSelectionState != null) {
            ctx.mutableCanvasState.value = ctx.mutableCanvasState.value.copy(selectionState = result.updatedSelectionState)
        }

        if (result.shouldUpdateHistory) {
            ctx.historyManager.updateHistoryCurrentState()
            ctx.refreshLayerState()
        }

        ctx.transitionTo(StandbyState)
    }

    override fun onTouchCancel(ctx: InteractionContext) {
        val tool = ctx.selectedTool as? StrokeTool ?: return
        ctx.strokeEngine.onStrokeCancel(tool)
        
        if (tool !is SelectionTool) {
            ctx.historyManager.restorePendingHistoryCapture()
        }
        
        ctx.transitionTo(StandbyState)
    }

    override fun onTap(row: Int, col: Int, ctx: InteractionContext) {}
    override fun commitPending(ctx: InteractionContext): Boolean = false
    override fun cancelPending(ctx: InteractionContext): Boolean = false
}
