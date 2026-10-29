package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.TimelineEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.Constants.PIXEL_DP
import com.instasprite.app.utils.pixelDp

@Composable
fun TimelineSheet(
    animation: Animation,
    layers: List<Layer>,
    activeFrameId: String,
    activeLayerId: String,
    isPlaying: Boolean,
    maxHeight: Dp,
    onEvent: (TimelineEvent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onLayerEvent: (LayerEvent) -> Unit = {},
    onionSkinEnabled: Boolean = false,
    reorderReset: Int = 0
) {
    val density = LocalDensity.current
    val pixelScale = PIXEL_DP.toFloat()
    var requestedHeight by rememberSaveable {
        mutableFloatStateOf((56 + layers.size.coerceIn(1, 3) * 16).toFloat())
    }
    val limit = maxHeight.coerceAtLeast(0.pixelDp)
    val minimum = 72.pixelDp.coerceAtMost(limit)
    val height = requestedHeight.pixelDp.coerceIn(minimum, limit)

    Surface(
        modifier
            .height(height)
            .fillMaxWidth(), color = AppTheme.colors.BackgroundColor
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(10.pixelDp)
                    .pointerInput(density, minimum, limit) {
                        detectVerticalDragGestures { change, amount ->
                            change.consume()
                            requestedHeight = (requestedHeight.coerceIn(
                                minimum.value / pixelScale,
                                limit.value / pixelScale
                            ) - amount / density.density / pixelScale)
                                .coerceIn(minimum.value / pixelScale, limit.value / pixelScale)
                        }
                    }, contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(22.pixelDp, 2.pixelDp)
                        .background(AppTheme.colors.Foreground2Color)
                )
            }
            AnimationTimeline(
                animation, layers, activeFrameId, activeLayerId, isPlaying,
                onEvent, onClose, Modifier.weight(1f), onLayerEvent = onLayerEvent,
                onionSkinEnabled = onionSkinEnabled, reorderReset = reorderReset
            )
        }
    }
}

@Preview(name = "Compact timeline sheet", widthDp = 400)
@Preview(name = "Narrow timeline sheet", widthDp = 320)
@Composable
private fun TimelineSheetPreview() {
    InstaSpriteTheme {
        TimelineSheet(
            TimelinePreviewData.animation,
            TimelinePreviewData.layers,
            "frame-2",
            "character",
            false,
            188.pixelDp,
            {},
            {})
    }
}

@Preview(name = "One-row minimum timeline", widthDp = 320)
@Composable
private fun MinimumTimelineSheetPreview() {
    InstaSpriteTheme {
        TimelineSheet(
            TimelinePreviewData.animation,
            TimelinePreviewData.layers,
            "frame-2",
            "effects",
            false,
            72.pixelDp,
            {},
            {})
    }
}
