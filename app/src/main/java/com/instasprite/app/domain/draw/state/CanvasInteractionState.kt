package com.instasprite.app.domain.draw.state

interface CanvasInteractionState {
    fun onTouchStart(row: Int, col: Int, ctx: InteractionContext)
    fun onTouchMove(row: Int, col: Int, ctx: InteractionContext)
    fun onTouchEnd(ctx: InteractionContext)
    fun onTouchCancel(ctx: InteractionContext)
    fun onTap(row: Int, col: Int, ctx: InteractionContext)
    fun commitPending(ctx: InteractionContext)
    fun cancelPending(ctx: InteractionContext)
}
