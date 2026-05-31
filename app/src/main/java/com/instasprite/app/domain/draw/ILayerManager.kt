package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.BlendMode

interface ILayerManager {
    fun addLayer(name: String)
    fun removeLayer(layerId: String)
    fun selectLayer(layerId: String)
    fun toggleLock(layerId: String)
    fun toggleVisibility(layerId: String)
    fun mergeLayerDown(layerId: String)
    fun reorderLayer(fromIndex: Int, toIndex: Int)
    fun setLayerOpacity(layerId: String, opacity: Float)
    fun setLayerBlendMode(layerId: String, mode: BlendMode)
}
