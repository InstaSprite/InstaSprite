package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

class AnimationDelegate(
    private val canvas: PixelCanvasUseCase,
    private val history: IHistoryManager,
    private val refresh: suspend () -> Unit
) {
    suspend fun insertFrame(empty: Boolean, linked: Boolean) =
        edit { canvas.insertFrame(empty, linked) }

    suspend fun appendFrame() {
        if (canvas.animation.frames.size >= Animation.MAX_FRAMES) return
        edit {
            canvas.selectFrame(canvas.animation.frames.last().id)
            canvas.insertFrame(empty = true)
        }
    }

    suspend fun deleteFrame() = edit { canvas.deleteFrame() }
    suspend fun setDuration(milliseconds: Int) = edit { canvas.setFrameDuration(milliseconds) }
    suspend fun clearCel() = edit { canvas.clearCel() }
    suspend fun copyPreviousCel(linked: Boolean) = edit { canvas.copyPreviousCel(linked) }
    suspend fun unlinkCel() = edit { canvas.unlinkCel() }
    suspend fun setPosterFrame() = edit { canvas.setPosterFrame() }

    private suspend fun edit(operation: () -> Unit) {
        history.recordOperationHistory(operation)
        refresh()
    }
}
