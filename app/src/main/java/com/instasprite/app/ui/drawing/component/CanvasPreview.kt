package com.instasprite.app.ui.drawing.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasPreview(
    imageBitmap: ImageBitmap,
    overlayImageBitmap: ImageBitmap?,
    canvasWidth: Int,
    canvasHeight: Int,
    modifier: Modifier = Modifier,
    previewSize: Dp = 64.pixelDp
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }

    var isExpanded by remember { mutableStateOf(false) }
    var sizeMultiplier by remember { mutableFloatStateOf(1f) }
    var isSliding by remember { mutableStateOf(false) }

    val aspectRatio = canvasWidth.toFloat() / canvasHeight.toFloat()

    // When sliding, collapse to show live preview at the new size
    val effectiveExpanded = isExpanded && !isSliding

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            containerWidth = coordinates.size.width.toFloat()
            containerHeight = coordinates.size.height.toFloat()
        }
    ) {
        val density = LocalDensity.current
        val scaledPreviewSize = previewSize * sizeMultiplier
        val previewWidthPx = with(density) { scaledPreviewSize.toPx() }
        val previewHeightPx = if (aspectRatio > 0f) previewWidthPx / aspectRatio else previewWidthPx

        val maxScale = if (previewWidthPx > 0) {
            minOf(containerWidth / previewWidthPx, containerHeight / previewHeightPx) * 0.95f
        } else 1f

        val targetScale = if (effectiveExpanded) (maxScale * 2).roundToInt() / 2f else 1f
        val animScale by animateFloatAsState(targetScale)

        val expandedOffsetX = (containerWidth - previewWidthPx) / 2f
        val expandedOffsetY = (containerHeight - previewHeightPx) / 2f

        val targetOffsetX = if (effectiveExpanded) expandedOffsetX else offsetX
        val targetOffsetY = if (effectiveExpanded) expandedOffsetY else offsetY

        val animOffsetX by animateFloatAsState(targetOffsetX)
        val animOffsetY by animateFloatAsState(targetOffsetY)

        val zoomState = rememberZoomState()

        if (isExpanded) {
            BackHandler {
                isExpanded = false
            }
            if (!isSliding) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.pixelDp)
                        .zIndex(5f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(AppTheme.colors.BackgroundColor)
                    ) {
                        Text(
                            text = "Preview",
                            color = AppTheme.colors.TextColorLight,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(4.pixelDp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = false },
                        shape = MaterialTheme.shapes.small,
                        colors = IconButtonColors(
                            containerColor = AppTheme.colors.BackgroundColor,
                            contentColor = Color.Unspecified,
                            disabledContainerColor = Color.Unspecified,
                            disabledContentColor = Color.Unspecified
                        ),
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_close,
                            tint = AppTheme.colors.DismissButtonColor
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 2.pixelDp)
                    .zIndex(5f)
                    .clip(MaterialTheme.shapes.small)
                    .background(AppTheme.colors.BackgroundColor)
            ) {
                Spacer(modifier = Modifier.width(2.pixelDp))

                PixelIcon(
                    icon = R.drawable.ic_resize,
                )

                Spacer(modifier = Modifier.width(2.pixelDp))

                Slider(
                    value = sizeMultiplier,
                    onValueChange = {
                        sizeMultiplier = it
                        isSliding = true
                    },
                    onValueChangeFinished = {
                        isSliding = false
                    },
                    valueRange = 0.5f..1.5f,
                    colors = AppTheme.colors.sliderColors(),
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            colors = AppTheme.colors.sliderColors(),
                            modifier = Modifier.height(6.pixelDp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(24.pixelDp)
                )

                Spacer(modifier = Modifier.width(2.pixelDp))

                Text(
                    text = "${(sizeMultiplier * 100).roundToInt()}%",
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.width(2.pixelDp))
            }
        }

        AnimatedVisibility(
            visible = effectiveExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.BackgroundColorDarker)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
            )
        }
        Box(
            modifier = Modifier
                .zIndex(if (effectiveExpanded) 1f else 0f)
                .offset {
                    IntOffset(animOffsetX.roundToInt(), animOffsetY.roundToInt())
                }
                .scale(animScale)
                .width(scaledPreviewSize)
                .aspectRatio(aspectRatio)
                .background(AppTheme.colors.Foreground0Color)
                .border(
                    (2 / animScale).pixelDp,
                    AppTheme.colors.BackgroundColor, RectangleShape
                )
                .clipToBounds()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((2 / animScale).pixelDp)
                    .zoomable(zoomState)
                    .drawCheckerboard(canvasWidth, canvasHeight)
            ) {
                val dstSize = IntSize(size.width.toInt(), size.height.toInt())
                drawImage(
                    image = imageBitmap,
                    dstOffset = IntOffset.Zero,
                    dstSize = dstSize,
                    filterQuality = FilterQuality.None
                )
                overlayImageBitmap?.let {
                    drawImage(
                        image = it,
                        dstOffset = IntOffset.Zero,
                        dstSize = dstSize,
                        filterQuality = FilterQuality.None
                    )
                }
            }

            if (!effectiveExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(
                            containerWidth,
                            containerHeight,
                            previewWidthPx,
                            previewHeightPx
                        ) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val maxX = (containerWidth - previewWidthPx).coerceAtLeast(0f)
                                val maxY = (containerHeight - previewHeightPx).coerceAtLeast(0f)
                                offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                                offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                            }
                        }
                        .clickable { isExpanded = true }
                )
            }
        }
    }
}

@Preview()
@Composable
private fun CanvasPreviewPreview() {
    val bitmap = ImageBitmap(32, 32)
    val overlay = ImageBitmap(32, 32)

    InstaSpriteTheme {
        Box(
            modifier = Modifier
                .size(300.pixelDp)
                .background(AppTheme.colors.Subtext0Color)
        ) {
            CanvasPreview(
                imageBitmap = bitmap,
                overlayImageBitmap = overlay,
                canvasWidth = 32,
                canvasHeight = 32,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}