package com.olaz.instasprite.ui.drawing.component

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.tool.selection.RectangleSelectionTool
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.drawCheckerboard
import com.olaz.instasprite.utils.drawSelectionOverlay
import com.olaz.instasprite.utils.drawingPointerInput

@Composable
fun PixelCanvas(
    modifier: Modifier = Modifier,
    pixelCanvasState: PixelCanvasState,
    bitmap: Bitmap?,
    overlayBitmap: Bitmap?,
    selectionBitmap: Bitmap?,
    selectedTool: Tool?,
    isSelectionAppendMode: Boolean,
    scale: Float,
    offset: Offset,
    onTransform: (Offset, Offset, Float, IntSize) -> Unit,
    onEvent: (PixelCanvasEvent) -> Unit
) {

    val canvasWidth = pixelCanvasState.width
    val canvasHeight = pixelCanvasState.height
    val drawVersion = pixelCanvasState.drawVersion
    val overlayVersion = pixelCanvasState.overlayVersion

    if (bitmap == null || canvasWidth <= 0 || canvasHeight <= 0) return

    val imageBitmap = remember(bitmap, drawVersion) {
        bitmap.asImageBitmap()
    }

    val overlayImageBitmap = remember(overlayBitmap, overlayVersion) {
        overlayBitmap?.asImageBitmap()
    }

    val selectionState = pixelCanvasState.selectionState
    val selectionImageBitmap = remember(selectionBitmap, selectionState) {
        selectionBitmap?.asImageBitmap()
    }

    val aspectRatio = canvasWidth.toFloat() / canvasHeight.toFloat()
    val borderSize = 5.dp

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .border(borderSize, CatppuccinUI.BackgroundColor)
                .padding(borderSize)
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(aspectRatio)
                    .fillMaxWidth(0.9f)
            ) {
                var canvasLayoutSize by remember { mutableStateOf(IntSize.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasLayoutSize = it }
                        .drawCheckerboard(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                        .drawingPointerInput(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            selectedTool = selectedTool,
                            scale = scale,
                            onEvent = onEvent,
                            onTransform = { centroid, pan, zoom ->
                                onTransform(centroid, pan, zoom, canvasLayoutSize)
                            }
                        )
                ) {
                    val dstSize = IntSize(size.width.toInt(), size.height.toInt())

                    drawImage(
                        image = imageBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = dstSize,
                        filterQuality = FilterQuality.None
                    )

                    selectionImageBitmap?.let {
                        drawImage(
                            image = it,
                            dstOffset = IntOffset.Zero,
                            dstSize = dstSize,
                            filterQuality = FilterQuality.None
                        )
                    }

                    overlayImageBitmap?.let {
                        drawImage(
                            image = it,
                            dstOffset = IntOffset.Zero,
                            dstSize = dstSize,
                            filterQuality = FilterQuality.None
                        )
                    }

                    if (selectionState != null) {
                        drawSelectionOverlay(
                            selectionState = selectionState,
                            showGrabHandle = (selectedTool is RectangleSelectionTool && !isSelectionAppendMode),
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            dstSize = dstSize,
                            scale = scale
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        PixelCanvas(
            pixelCanvasState = PixelCanvasState(
                width = 16,
                height = 16
            ),
            bitmap = createBitmap(16, 16),
            selectedTool = PencilTool,
            isSelectionAppendMode = false,
            scale = 1f,
            offset = Offset.Zero,
            onTransform = { _, _, _, _ -> },
            overlayBitmap = null,
            selectionBitmap = null,
            onEvent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}