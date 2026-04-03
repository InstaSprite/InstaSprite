package com.olaz.instasprite.ui.drawing.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.olaz.instasprite.domain.model.Cel
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.ui.drawing.contract.LayerEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.drawCheckerboard


@Composable
fun LayerView(
    layer: Layer,
    onEvent: (LayerEvent) -> Unit,
    canvasWidth: Int,
    canvasHeight: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    onVisibilityToggle: () -> Unit,
    onLockToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {

    val backgroundColor = if (isActive)
        CatppuccinUI.BackgroundColor
    else
        CatppuccinUI.BackgroundColorDarker

    val bitmapImage = remember(layer.cel.pixels, canvasWidth, canvasHeight) {
        if (canvasWidth > 0 && canvasHeight > 0 && layer.cel.pixels.size == canvasWidth * canvasHeight) {
            val bitmap = createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val argbPixels = layer.cel.pixels
            bitmap.setPixels(argbPixels, 0, canvasWidth, 0, 0, canvasWidth, canvasHeight)
            bitmap.asImageBitmap()
        } else {
            null
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
            .clickable(enabled = true, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(1f)
                .background(CatppuccinUI.BackgroundColorDarker),
            contentAlignment = Alignment.Center
        ) {
            if (bitmapImage != null) {
                Image(
                    bitmap = bitmapImage,
                    contentDescription = "Layer Preview",
                    contentScale = ContentScale.FillWidth,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawCheckerboard(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = layer.name,
                    color = CatppuccinUI.TextColorLight,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Layer",
                        tint = CatppuccinUI.DismissButtonColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onVisibilityToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = CatppuccinUI.TextColorLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onLockToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Outlined.LockOpen,
                        contentDescription = "Toggle Lock",
                        tint = CatppuccinUI.TextColorLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { onEvent(LayerEvent.MergeLayerDown(layer.id)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Merge Down",
                        tint = CatppuccinUI.TextColorLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }
        }
    }
}

@Preview
@Composable
private fun PreviewActive() {
    InstaSpriteTheme() {
        LayerView(
            layer = Layer(
                id = "test1",
                name = "Layer 1",
                cel = Cel(
                    x = 0,
                    y = 0,
                    width = 16,
                    height = 16,
                    pixels = IntArray(16 * 16) {
                        CatppuccinUI.CurrentPalette.Flamingo.toArgb()
                    }
                )
            ),
            onEvent = {},
            canvasWidth = 16,
            canvasHeight = 16,
            isActive = true,
            onClick = {},
            onVisibilityToggle = {},
            onLockToggle = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun PreviewInactive() {
    InstaSpriteTheme() {
        LayerView(
            layer = Layer(
                id = "test1",
                name = "Layer 1",
                cel = Cel(
                    x = 0,
                    y = 0,
                    width = 16,
                    height = 16,
                    pixels = IntArray(16 * 16) {
                        CatppuccinUI.CurrentPalette.Maroon.toArgb()
                    }
                )
            ),
            onEvent = {},
            canvasWidth = 16,
            canvasHeight = 16,
            isActive = false,
            onClick = {},
            onVisibilityToggle = {},
            onLockToggle = {},
            onDelete = {}
        )
    }
}