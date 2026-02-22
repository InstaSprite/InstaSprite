package com.olaz.instasprite.ui.drawing.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.olaz.instasprite.domain.tool.Tool
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasEvent
import com.olaz.instasprite.ui.drawing.contract.PixelCanvasState
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.drawCheckerboard
import com.olaz.instasprite.utils.drawingPointerInput
import com.olaz.instasprite.utils.updateBitmapPixels

@Composable
fun PixelCanvas(
    modifier: Modifier = Modifier,
    pixelCanvasState: PixelCanvasState,
    selectedTool: Tool?,
    onEvent: (PixelCanvasEvent) -> Unit
) {

    val (canvasWidth, canvasHeight, pixels) = pixelCanvasState

    val bitmap = remember(canvasWidth, canvasHeight) {
        createBitmap(canvasWidth, canvasHeight)
    }

    val imageBitmap: ImageBitmap = remember(bitmap, pixels) {
        if (canvasWidth > 0 && canvasHeight > 0 && pixels.isNotEmpty()) {
            updateBitmapPixels(
                bitmap = bitmap,
                pixels = pixels
            )
        }
        bitmap.asImageBitmap()
    }

    // Store the stroke and color for the grid overlay once, maybe optimize memory
    val gridStroke = remember { Stroke(width = 1f) }
    val gridColor = remember { Color.LightGray.copy(alpha = 0.2f) }

    val aspectRatio = canvasWidth.toFloat() / canvasHeight.toFloat()
    val borderSize = 5.dp

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .border(borderSize, CatppuccinUI.BackgroundColor)
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
                        .drawCheckerboard(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                        .drawingPointerInput(canvasWidth, canvasHeight, selectedTool, onEvent)
                ) {
                    drawImage(
                        image = imageBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                        filterQuality = FilterQuality.None
                    )

//                    if (canvasWidth < 32 && canvasHeight < 32) {
//                        drawGridOverlay(canvasWidth, canvasHeight, gridColor, gridStroke)
//                    }
                }
            }
        }
    }
}
