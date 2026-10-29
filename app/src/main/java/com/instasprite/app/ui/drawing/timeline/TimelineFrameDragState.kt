package com.instasprite.app.ui.drawing.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.instasprite.app.R
import com.instasprite.app.domain.model.AnimationFrame
import com.instasprite.app.ui.drawing.contract.TimelineEvent

internal class TimelineFrameDragState(initial: List<AnimationFrame>) {
    private var resetVersion = 0
    private var source = initial
    private var original = initial
    private val pending = mutableListOf<List<AnimationFrame>>()
    var frames by mutableStateOf(initial)
        private set
    private var draggedFrameId: String? = null

    fun sync(updated: List<AnimationFrame>, reset: Int = 0) {
        if (reset != resetVersion) {
            resetVersion = reset
            source = updated
            pending.clear()
            draggedFrameId = null
            frames = updated
            return
        }
        val acknowledged = pending.indexOfLast { it == updated }
        if (acknowledged >= 0) {
            pending.subList(0, acknowledged + 1).clear()
            source = updated
            if (pending.isNotEmpty() || draggedFrameId != null) return
        } else {
            if (updated == source) return
            source = updated
            pending.clear()
        }
        draggedFrameId = null
        frames = updated
    }

    fun start(frameId: String) {
        if (frames.none { it.id == frameId }) return
        original = frames
        draggedFrameId = frameId
    }

    fun move(fromKey: Any, toKey: Any) {
        if (draggedFrameId == null) return
        val from = frames.indexOfFirst { it.id == fromKey }
        val to = frames.indexOfFirst { it.id == toKey }
        if (from < 0 || to < 0 || from == to) return
        frames = frames.toMutableList().apply { add(to, removeAt(from)) }
    }

    fun stop(): TimelineEvent.Reorder? {
        val id = draggedFrameId
        draggedFrameId = null
        val index = frames.indexOfFirst { it.id == id }
        return if (id != null && index >= 0 && original.indexOfFirst { it.id == id } != index) {
            pending.add(frames)
            TimelineEvent.Reorder(id, index)
        } else null
    }
}

@Composable
internal fun Modifier.frameReorderActions(
    frameId: String,
    index: Int,
    count: Int,
    enabled: Boolean,
    onReorder: (TimelineEvent.Reorder) -> Unit
): Modifier {
    val earlier = stringResource(R.string.animation_move_earlier)
    val later = stringResource(R.string.animation_move_later)
    return semantics {
        customActions = buildList {
            if (enabled && index > 0) add(CustomAccessibilityAction(earlier) {
                onReorder(TimelineEvent.Reorder(frameId, index - 1)); true
            })
            if (enabled && index < count - 1) add(CustomAccessibilityAction(later) {
                onReorder(TimelineEvent.Reorder(frameId, index + 1)); true
            })
        }
    }
}
