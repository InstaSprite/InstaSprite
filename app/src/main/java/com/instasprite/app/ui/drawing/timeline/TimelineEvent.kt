package com.instasprite.app.ui.drawing.timeline

sealed interface TimelineEvent {
    data class Select(val frameId: String, val layerId: String? = null) : TimelineEvent
    data class Insert(val empty: Boolean = false, val linked: Boolean = false) : TimelineEvent
    data class Duration(val milliseconds: Int) : TimelineEvent
    data object Delete : TimelineEvent
    data object ClearCel : TimelineEvent
    data object CopyPrevious : TimelineEvent
    data object LinkPrevious : TimelineEvent
    data object Unlink : TimelineEvent
    data object SetPoster : TimelineEvent
    data object TogglePlayback : TimelineEvent
}
