package com.instasprite.app.domain.draw

import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import com.instasprite.app.domain.usecase.PixelCanvasUseCase

class DrawingSession internal constructor(
    val canvas: PixelCanvasUseCase,
    val engine: DrawingEngine,
    private val historyStore: HistoryDiskStore
) {
    fun clearHistory() {
        historyStore.destroy()
    }

    fun close() {
        try {
            engine.release()
        } finally {
            clearHistory()
        }
    }
}
