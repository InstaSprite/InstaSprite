package com.instasprite.app.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal fun DrawingNavigationEffect(
    destination: DrawingDestination?,
    onHandled: (DrawingDestination) -> Unit,
    onNavigate: (String, Int, Int, String?, Int?) -> Unit
) {
    val handleDestination by rememberUpdatedState(onHandled)
    val navigate by rememberUpdatedState(onNavigate)
    LaunchedEffect(destination) {
        destination?.let {
            handleDestination(it)
            navigate(it.spriteId, it.width, it.height, it.name, it.paletteId)
        }
    }
}
