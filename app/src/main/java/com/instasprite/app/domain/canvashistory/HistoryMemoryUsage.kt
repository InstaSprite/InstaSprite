package com.instasprite.app.domain.canvashistory

import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.AnimationCel
import com.instasprite.app.domain.model.AnimationFrame
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.model.Sprite
import java.util.IdentityHashMap

internal class HistoryMemoryUsage {
    private val references = IdentityHashMap<Any, Int>()
    var bytes = 0L
        private set

    fun retain(value: Any?) = update(value, true)

    fun release(value: Any?) = update(value, false)

    fun clear() {
        references.clear()
        bytes = 0
    }

    private fun update(value: Any?, retain: Boolean) {
        if (value == null) return
        val count = references[value] ?: 0
        if (retain) {
            references[value] = count + 1
            if (count > 0) return
            bytes += sizeOf(value)
        } else {
            check(count > 0)
            if (count > 1) {
                references[value] = count - 1
                return
            }
            references.remove(value)
            bytes -= sizeOf(value)
        }
        when (value) {
            is CelEditEntry -> {
                update(value.layerId, retain)
                update(value.frameId, retain)
                update(value.beforeCel, retain)
                update(value.afterCel, retain)
                update(value.beforeImagePatch, retain)
                update(value.afterImagePatch, retain)
                update(value.beforeSelection, retain)
                update(value.afterSelection, retain)
            }

            is UndoEntry -> {
                update(value.deltas, retain)
                update(value.beforeSelection, retain)
                update(value.afterSelection, retain)
            }

            is OperationEntry -> {
                update(value.before, retain)
                update(value.after, retain)
            }

            is TileDelta -> {
                update(value.layerId, retain)
                update(value.coord, retain)
                update(value.before, retain)
            }

            is HistoryCanvasState -> {
                update(value.layers, retain)
                update(value.sprite, retain)
                update(value.selectionState, retain)
                update(value.activeLayerId, retain)
                update(value.activeFrameId, retain)
            }

            is Sprite -> {
                update(value.id, retain)
                update(value.layers, retain)
                update(value.animation, retain)
                update(value.colorPalette, retain)
            }

            is Animation -> {
                update(value.frames, retain)
                update(value.cels, retain)
                update(value.images, retain)
                update(value.posterFrameId, retain)
            }

            is AnimationCel -> {
                update(value.id, retain)
                update(value.layerId, retain)
                update(value.frameId, retain)
                update(value.dataId, retain)
            }

            is AnimationFrame -> update(value.id, retain)
            is CelImage -> {
                update(value.id, retain)
                update(value.tiles, retain)
            }

            is Layer -> {
                update(value.id, retain)
                update(value.name, retain)
                update(value.tiles, retain)
            }

            is SelectionState -> update(value.mask, retain)
            is Map<*, *> -> value.forEach { (key, item) ->
                update(key, retain)
                update(item, retain)
            }

            is List<*> -> value.forEach { update(it, retain) }
        }
    }

    private fun sizeOf(value: Any): Long = 64L + when (value) {
        is IntArray -> 24L + value.size * 4L
        is BooleanArray -> 24L + value.size
        is String -> 40L + value.length * 2L
        is Map<*, *> -> 64L + value.size * 64L
        is List<*> -> 32L + value.size * 8L
        else -> 64L
    }
}
