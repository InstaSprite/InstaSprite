package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun AsyncImageZoomableOverlay(
    model: Any?,
    onDismiss: () -> Unit,
) {
    val zoomState = rememberZoomState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .zoomable(zoomState),
                contentScale = ContentScale.FillWidth,
                filterQuality = FilterQuality.None
            )
        }
    }
}