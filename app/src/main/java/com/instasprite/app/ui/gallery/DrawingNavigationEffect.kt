package com.instasprite.app.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@Composable
internal fun DrawingNavigationEffect(
    state: StateFlow<GalleryState>,
    onHandled: (DrawingDestination) -> Unit,
    onNavigate: (String, Int, Int, String?, Int?) -> Unit
) {
    val handleDestination by rememberUpdatedState(onHandled)
    val navigate by rememberUpdatedState(onNavigate)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, state) {
        collectDrawingNavigation(lifecycle, state, { handleDestination(it) }) {
            navigate(it.spriteId, it.width, it.height, it.name, it.paletteId)
        }
    }
}

internal suspend fun collectDrawingNavigation(
    lifecycle: Lifecycle,
    state: StateFlow<GalleryState>,
    onHandled: (DrawingDestination) -> Unit,
    onNavigate: (DrawingDestination) -> Unit
) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        state.map { it.drawingDestination }.filterNotNull().collect { destination ->
            onNavigate(destination)
            onHandled(destination)
        }
    }
}
