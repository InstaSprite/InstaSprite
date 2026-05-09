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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.tool.PencilTool
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.domain.tool.selection.RectangleSelectionTool
import com.olaz.instasprite.ui.drawing.contract.CursorDrawEvent
import com.olaz.instasprite.ui.drawing.contract.CursorState
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.cursorPointerInput
import com.olaz.instasprite.utils.drawCheckerboard
import com.olaz.instasprite.utils.drawSelectionOverlay
import com.olaz.instasprite.utils.drawingPointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.olaz.instasprite.domain.tool.EyedropperTool
import com.olaz.instasprite.utils.drawCursorOverlay

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
    isCursorMode: Boolean = false,
    cursorState: CursorState = CursorState(),
    toolSize: Int = 1,
    activeColor: Color = Color.White,
    onCursorDrawEvent: (CursorDrawEvent) -> Unit = {},
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

    var canvasLayoutSize by remember { mutableStateOf(IntSize.Zero) }

    val context = LocalContext.current
    val toolIconBitmap = remember(selectedTool) {
        when (selectedTool) {
            is PencilTool, is EyedropperTool -> ContextCompat.getDrawable(context, selectedTool.icon)
                ?.toBitmap(64, 64)
                ?.asImageBitmap()
            else -> null
        }
    }

    val pointerInputModifier = if (isCursorMode) {
        Modifier.cursorPointerInput(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            cursorState = cursorState,
            scale = scale,
            onCursorMove = { cursorX, cursorY ->
                onCursorDrawEvent(CursorDrawEvent.MoveCursor(cursorX, cursorY))
            },
            onTransform = { centroid, pan, zoom ->
                onTransform(centroid, pan, zoom, canvasLayoutSize)
            }
        )
    } else {
        Modifier.drawingPointerInput(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            selectedTool = selectedTool,
            scale = scale,
            onEvent = onEvent,
            onTransform = { centroid, pan, zoom ->
                onTransform(centroid, pan, zoom, canvasLayoutSize)
            }
        )
    }

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
//                .border(borderSize, CatppuccinUI.BackgroundColor)
                .drawBehind(onDraw = {
                    drawRect(
                        color = CatppuccinUI.BackgroundColor
                    )
                })
                .padding(borderSize)
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(aspectRatio)
                    .fillMaxWidth(0.9f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasLayoutSize = it }
                        .drawCheckerboard(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                        .then(pointerInputModifier)
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

                    if (isCursorMode && cursorState.isVisible) {
                        drawCursorOverlay(
                            cursorState = cursorState,
                            selectedTool = selectedTool,
                            toolSize = toolSize,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            dstSize = dstSize,
                            activeColor = activeColor,
                            scale = scale,
                            toolIconBitmap = toolIconBitmap
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
    InstaSpriteTheme {
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

@Preview
@Composable
private fun PreviewCursorMode() {
    InstaSpriteTheme {
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
            isCursorMode = true,
            cursorState = CursorState(cursorX = 8.5f, cursorY = 8.5f, isVisible = true),
            toolSize = 2,
            activeColor = Color.Red,
            onTransform = { _, _, _, _ -> },
            overlayBitmap = null,
            selectionBitmap = null,
            onEvent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}