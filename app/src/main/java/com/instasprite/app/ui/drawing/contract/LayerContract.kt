package com.instasprite.app.ui.drawing.contract


// NOTE: Layer in ui are reverse -> any operation using index should be taken this into account
sealed interface LayerEvent {
    data object AddLayer : LayerEvent
    data class SelectLayer(val layerId: String) : LayerEvent
    data class ToggleVisibility(val layerId: String) : LayerEvent
    data class ToggleLock(val layerId: String) : LayerEvent
    data class DeleteLayer(val layerId: String) : LayerEvent
    data class MergeLayerDown(val layerId: String) : LayerEvent
    data class ReorderLayer(val fromIndex: Int, val toIndex: Int) : LayerEvent
}
