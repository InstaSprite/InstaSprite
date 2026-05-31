package com.instasprite.app.domain.draw.state

import androidx.compose.ui.graphics.Color
import com.instasprite.app.domain.draw.BitmapManager
import com.instasprite.app.domain.draw.StrokeEngine
import com.instasprite.app.domain.draw.IHistoryManager
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.ui.drawing.contract.PixelCanvasState
import kotlinx.coroutines.flow.MutableStateFlow

interface InteractionContext {
    val pixelCanvasUseCase: PixelCanvasUseCase
    val bitmapManager: BitmapManager
    val historyManager: IHistoryManager
    val strokeEngine: StrokeEngine
    val mutableCanvasState: MutableStateFlow<PixelCanvasState>
    
    val selectedTool: Tool
    val activeColor: Color
    val toolSize: Int
    val brushShape: BrushShape
    val isAppendSelectionMode: Boolean
    val zoomScale: Float

    fun transitionTo(newState: CanvasInteractionState)
}
