package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LayerDelegate(
    private val pixelCanvasUseCase: PixelCanvasUseCase,
    private val historyManager: IHistoryManager,
    private val refreshLayerState: () -> Unit,
    private val refreshActiveLayerState: () -> Unit,
    private val syncStateVersions: () -> Unit,
    private val scope: CoroutineScope,
    private val bitmapManager: BitmapManager
) : ILayerManager {

    override fun addLayer(name: String) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.addLayer(name)
        }
        refreshLayerState()
        refreshActiveLayerState()
    }

    override fun removeLayer(layerId: String) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.removeLayer(layerId)
        }
        refreshLayerState()
        refreshActiveLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun selectLayer(layerId: String) {
        pixelCanvasUseCase.setActiveLayer(layerId)
        refreshActiveLayerState()
    }

    override fun toggleLock(layerId: String) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.toggleLock(layerId)
        }
        refreshLayerState()
    }

    override fun toggleVisibility(layerId: String) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.toggleVisibility(layerId)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun mergeLayerDown(layerId: String) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.mergeLayerDown(layerId)
        }
        refreshLayerState()
        refreshActiveLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun reorderLayer(fromIndex: Int, toIndex: Int) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.reorderLayer(fromIndex = fromIndex, toIndex = toIndex)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun setLayerOpacity(layerId: String, opacity: Float) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.setLayerOpacity(layerId, opacity)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }

    override fun setLayerBlendMode(layerId: String, mode: BlendMode) {
        historyManager.recordOperationHistory {
            pixelCanvasUseCase.setLayerBlendMode(layerId, mode)
        }
        refreshLayerState()
        scope.launch {
            bitmapManager.refreshBitmapState()
            syncStateVersions()
        }
    }
}
