package com.instasprite.app.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.instasprite.app.R
import com.instasprite.app.ui.drawing.tutorial.DrawingTutorialStep
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp
import kotlin.math.roundToInt

@Composable
fun SpotlightOverlay(
    currentStep: TutorialStep,
    activeRect: Rect,
    stepIndex: Int,
    stepCount: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var overlayBounds by remember { mutableStateOf(Rect.Zero) }
    var popupHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val shape = MaterialTheme.shapes.small
    val scroll = key(currentStep) { rememberScrollState() }
    BackHandler { if (stepIndex > 0) onBack() else onSkip() }
    BoxWithConstraints(
        modifier.fillMaxSize().safeDrawingPadding().onGloballyPositioned { overlayBounds = it.boundsInRoot() }
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        val relative = activeRect.translate(-overlayBounds.left, -overlayBounds.top)
        Canvas(Modifier.fillMaxSize()) {
            val layer = drawContext.canvas.nativeCanvas.saveLayer(null, null)
            drawRect(Color.Black.copy(alpha = 0.78f))
            if (activeRect != Rect.Zero && overlayBounds != Rect.Zero) {
                val padding = 3.pixelDp.toPx()
                val outline = shape.createOutline(
                    androidx.compose.ui.geometry.Size(relative.width + padding * 2, relative.height + padding * 2),
                    direction, this
                )
                translate(relative.left - padding, relative.top - padding) {
                    drawOutline(outline, Color.Transparent, blendMode = BlendMode.Clear)
                }
            }
            drawContext.canvas.nativeCanvas.restoreToCount(layer)
        }
        val margin = 8.pixelDp
        val availableHeight = (maxHeight - margin * 2).coerceAtLeast(1.pixelDp)
        Column(
            Modifier.fillMaxWidth().padding(horizontal = margin)
                .heightIn(max = availableHeight)
                .onGloballyPositioned { popupHeight = it.size.height }
                .offset {
                    val gap = with(density) { margin.toPx() }.roundToInt()
                    val bottom = (overlayBounds.height.roundToInt() - popupHeight - gap).coerceAtLeast(gap)
                    val y = if (activeRect == Rect.Zero) bottom / 2
                        else if (overlayBounds.height - relative.bottom > relative.top) relative.bottom.roundToInt() + gap
                        else relative.top.roundToInt() - popupHeight - gap
                    IntOffset(0, y.coerceIn(gap, bottom))
                }
                .background(AppTheme.colors.DialogColor, shape)
                .padding(8.pixelDp)
        ) {
            Column(Modifier.weight(1f, fill = false).verticalScroll(scroll)) {
                Text(stringResource(R.string.tut_progress, stepIndex + 1, stepCount),
                    color = AppTheme.colors.Subtext0Color, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(currentStep.titleRes), color = AppTheme.colors.LinkColor,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.pixelDp))
                Text(stringResource(currentStep.descriptionRes), color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSkip) { Text(stringResource(R.string.tut_btn_skip), color = AppTheme.colors.Subtext0Color) }
                TextButton(onClick = onBack, enabled = stepIndex > 0) {
                    Text(stringResource(R.string.tut_btn_back), color = if (stepIndex > 0) AppTheme.colors.TextColorLight else AppTheme.colors.Subtext0Color)
                }
                TextButton(onClick = onNext) {
                    Text(stringResource(if (stepIndex == stepCount - 1) R.string.tut_btn_finish else R.string.tut_btn_next),
                        color = AppTheme.colors.LinkColor)
                }
            }
        }
    }
}

@Preview(name = "Timeline tutorial", widthDp = 360, heightDp = 640)
@Preview(name = "Short tutorial", widthDp = 640, heightDp = 320, fontScale = 1.3f)
@Composable
private fun SpotlightOverlayPreview() {
    InstaSpriteTheme {
        SpotlightOverlay(DrawingTutorialStep.TIMELINE_CELS, Rect.Zero, 9, 12, {}, {}, {})
    }
}
