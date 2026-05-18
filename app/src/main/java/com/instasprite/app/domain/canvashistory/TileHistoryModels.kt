package com.instasprite.app.domain.canvashistory

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.model.TileCoord

sealed interface HistoryEntry {
    fun isEmpty(): Boolean
}

@Suppress("ArrayInDataClass")
data class TileDelta(
    val layerId: String,
    val coord: TileCoord,
    val before: IntArray
)

data class UndoEntry(
    val deltas: List<TileDelta>,
    val beforeSelection: SelectionState? = null,
    val afterSelection: SelectionState? = null
) : HistoryEntry {
    override fun isEmpty(): Boolean = deltas.isEmpty() && beforeSelection == afterSelection
}

data class HistoryCanvasState(
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val activeLayerId: String,
    val selectionState: SelectionState? = null
)

data class OperationEntry(
    val before: HistoryCanvasState,
    val after: HistoryCanvasState
) : HistoryEntry {
    override fun isEmpty(): Boolean = before == after
}

enum class TransformType {
    ROTATE_CW,
    ROTATE_CCW,
    FLIP_H,
    FLIP_V
}

data class TransformEntry(
    val transform: TransformType
) : HistoryEntry {
    override fun isEmpty(): Boolean = false
}
