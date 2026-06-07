package com.instasprite.app.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp
import kotlin.math.roundToInt

@Composable
fun SpotlightOverlay(
    currentStep: TutorialStep,
    activeRect: Rect,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var overlayBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val overlayShape = MaterialTheme.shapes.small

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    overlayBounds = coords.boundsInRoot()
                }
        ) {
            val layerId = drawContext.canvas.nativeCanvas.saveLayer(null, null)

            drawRect(
                color = Color.Black.copy(alpha = 0.9f),
                size = size
            )

            if (activeRect != Rect.Zero && overlayBounds != Rect.Zero) {
                // Offset the target rect by the overlay's own position in root
                val relativeRect = activeRect.translate(-overlayBounds.left, -overlayBounds.top)

                val padding = 4.pixelDp.toPx()
                val targetSize = androidx.compose.ui.geometry.Size(
                    relativeRect.width + padding * 2,
                    relativeRect.height + padding * 2
                )

                val outline = overlayShape.createOutline(
                    size = targetSize,
                    layoutDirection = layoutDirection,
                    density = this
                )

                val path = (outline as Outline.Generic).path

                translate(
                    left = relativeRect.left - padding,
                    top = relativeRect.top - padding
                ) {
                    drawPath(
                        path = path,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            }

            drawContext.canvas.nativeCanvas.restoreToCount(layerId)
        }

        val density = LocalDensity.current
        val config = LocalConfiguration.current

        if (activeRect != Rect.Zero) {
            val title = stringResource(currentStep.titleRes)
            val description = stringResource(currentStep.descriptionRes)

            val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }
            val spaceBelow = screenHeight - activeRect.bottom
            val spaceAbove = activeRect.top

            val isBelow = spaceBelow > spaceAbove
            val relativeRect = if (overlayBounds != Rect.Zero) {
                activeRect.translate(-overlayBounds.left, -overlayBounds.top)
            } else {
                activeRect
            }

            var popupHeight by remember { mutableIntStateOf(0) }
            val navBarInsets = WindowInsets.systemBars

            Box(
                modifier = Modifier
                    .onGloballyPositioned { popupHeight = it.size.height }
                    .offset {
                        val navBarHeight = navBarInsets.getBottom(density)
                        val maxBottomY = screenHeight.roundToInt() - popupHeight - 24.pixelDp.toPx()
                            .roundToInt() - navBarHeight
                        val yOffset = if (isBelow) {
                            (relativeRect.bottom + 20.pixelDp.toPx()).roundToInt()
                        } else {
                            (relativeRect.top - popupHeight - 20.pixelDp.toPx()).roundToInt()
                        }

                        // Fallback clamp if popupHeight is 0 (initial frame)
                        val safeMax = maxOf(24.pixelDp.toPx().roundToInt(), maxBottomY)
                        IntOffset(
                            x = 0,
                            y = yOffset.coerceIn(24.pixelDp.toPx().roundToInt(), safeMax)
                        )
                    }
                    .padding(horizontal = 24.pixelDp)
                    .background(AppTheme.colors.BackgroundColor, MaterialTheme.shapes.small)
                    .padding(8.pixelDp)
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.LinkColor
                    )
                    Spacer(modifier = Modifier.height(4.pixelDp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.TextColorLight
                    )
                    Spacer(modifier = Modifier.height(16.pixelDp))

                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Button(
                            onClick = onSkip,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = AppTheme.colors.TextColorLight
                            )
                        ) {
                            Text(stringResource(R.string.tut_btn_skip))
                        }
                        Button(
                            onClick = onNext,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.AccentButtonColor,
                                contentColor = Color.White
                            )
                        ) {
                            val btnText = if (isLastStep) stringResource(R.string.tut_btn_finish) else stringResource(R.string.tut_btn_next)
                            Text(text = btnText, color = AppTheme.colors.TextColorDark)
                        }
                    }
                }
            }
        }
    }
}
