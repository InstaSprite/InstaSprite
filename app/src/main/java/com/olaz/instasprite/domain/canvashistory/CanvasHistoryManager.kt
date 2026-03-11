package com.olaz.instasprite.domain.canvashistory

import java.util.ArrayDeque

class CanvasHistoryManager<T> {
    private val undoStack: ArrayDeque<T> = ArrayDeque()
    private val redoStack: ArrayDeque<T> = ArrayDeque()
    private var currentState: T? = null

    fun saveState(state: T) {
        currentState?.let { undoStack.addLast(it) }
        currentState = state
        redoStack.clear()
    }


    fun undo(): T? {
        if (undoStack.isNotEmpty()) {
            currentState?.let { redoStack.addLast(it) }
            currentState = undoStack.removeLast()
            return currentState
        }
        return null
    }

    fun redo(): T? {
        if (redoStack.isNotEmpty()) {
            currentState?.let { undoStack.addLast(it) }
            currentState = redoStack.removeLast()
            return currentState
        }
        return null
    }

    fun getUndoStack(): ArrayDeque<T> {
        return undoStack
    }

    fun getRedoStack(): ArrayDeque<T> {
        return redoStack
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        currentState = null
    }

    fun setCurrentState(state: T) {
        currentState = state
    }
}
