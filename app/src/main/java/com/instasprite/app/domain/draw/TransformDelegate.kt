package com.instasprite.app.domain.draw

import com.instasprite.app.domain.canvashistory.TransformType
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TransformDelegate(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    private val historyManager: IHistoryManager,
    private val refreshCanvasSizeState: () -> Unit,
    private val refreshLayerState: () -> Unit,
    private val scope: CoroutineScope,
    private val bitmapManager: BitmapManager,
    private val syncStateVersions: () -> Unit
) : ITransformManager {

    override fun rotate() {
        historyManager.recordTransformHistory(TransformType.ROTATE_CW) {
            pixelCanvasUseCase.rotateCanvas()
        }
        refreshCanvasSizeState()
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun hFlip() {
        historyManager.recordTransformHistory(TransformType.FLIP_H) {
            pixelCanvasUseCase.hFlipCanvas()
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun vFlip() {
        historyManager.recordTransformHistory(TransformType.FLIP_V) {
            pixelCanvasUseCase.vFlipCanvas()
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun resizeCanvas(width: Int, height: Int) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.resizeCanvas(width, height)
        }
        refreshCanvasSizeState()
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }
}
