package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState

data class DrawingCanvasState(
    val width: Int = 0,
    val height: Int = 0,
    val drawVersion: Long = 0,
    val overlayVersion: Long = 0,
    val layers: List<Layer> = emptyList(),
    val activeLayerId: String = "",
    val selectionState: SelectionState? = null
)
