package com.instasprite.app.domain.draw

import com.instasprite.app.domain.canvashistory.TransformType

interface IHistoryManager {
    fun saveState()
    fun undo()
    fun redo()
    fun resetHistory()
    fun discardHistoryCapture()
    fun updateHistoryCurrentState()
    fun restorePendingHistoryCapture()
    fun recordOperationHistory(operation: () -> Unit)
    fun recordTransformHistory(transform: TransformType, operation: () -> Unit)
}
