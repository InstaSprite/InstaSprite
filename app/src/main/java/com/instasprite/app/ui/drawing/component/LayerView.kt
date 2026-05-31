package com.instasprite.app.ui.drawing.component

import com.instasprite.app.utils.pixelDp

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.instasprite.app.R
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.dialog.LayerOptionsDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.inflateCel


@OptIn(ExperimentalMaterial3Api::class)
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
        AppTheme.colors.BackgroundColor
    else
        AppTheme.colors.BackgroundColorDarker

    val bitmapImage = remember(layer.cel, canvasWidth, canvasHeight) {
        if (canvasWidth > 0 && canvasHeight > 0) {
            val bitmap = createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val argbPixels = inflateCel(layer.cel, canvasWidth, canvasHeight)
            bitmap.setPixels(argbPixels, 0, canvasWidth, 0, 0, canvasWidth, canvasHeight)
            bitmap.asImageBitmap()
        } else {
            null
        }
    }

    var localOpacity by remember(layer.opacity) { mutableFloatStateOf(layer.opacity) }

    Column(
        modifier = Modifier.background(backgroundColor)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(6.pixelDp)
                .clickable(enabled = true, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(66.pixelDp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapImage != null) {
                    Image(
                        bitmap = bitmapImage,
                        contentDescription = stringResource(R.string.layer_preview),
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

            Spacer(modifier = Modifier.width(6.pixelDp))

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().height(66.pixelDp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = layer.name,
                        color = AppTheme.colors.TextColorLight,
                        fontSize = 14.sp
                    )
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(22.pixelDp)
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_trash,
                            contentDescription = stringResource(R.string.delete_layer),
                            tint = AppTheme.colors.DismissButtonColor,
                        )
                    }

                    if (showDeleteConfirm) {
                        ConfirmationDialog(
                            title = stringResource(R.string.delete_layer),
                            text = stringResource(R.string.are_you_sure_you_want_to_delete),
                            highlightText = layer.name,
                            confirmButtonText = stringResource(R.string.delete),
                            dismissButtonText = stringResource(R.string.cancel),
                            highlightTextColor = AppTheme.colors.DismissButtonColor,
                            hasQuestionMark = true,
                            onConfirm = {
                                showDeleteConfirm = false
                                onDelete()
                            },
                            onDismiss = {
                                showDeleteConfirm = false
                            }
                        )
                    }
                }

                Text(
                    text = layer.blendMode.toString().uppercase(),
                    color = AppTheme.colors.TextColorLight,
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onVisibilityToggle,
                        modifier = Modifier.size(22.pixelDp)
                    ) {
                        PixelIcon(
                            icon = if (layer.isVisible) R.drawable.ic_visible_on else R.drawable.ic_visible_off,
                            contentDescription = stringResource(R.string.toggle_visibility),
                            tint = AppTheme.colors.TextColorLight,
                        )
                    }
                    IconButton(
                        onClick = onLockToggle,
                        modifier = Modifier.size(22.pixelDp)
                    ) {
                        PixelIcon(
                            icon = if (layer.isLocked) R.drawable.ic_lock else R.drawable.ic_lock_unlock,
                            contentDescription = stringResource(R.string.toggle_lock),
                            tint = AppTheme.colors.TextColorLight,
                            modifier = Modifier.size(14.pixelDp)
                        )
                    }
                    var showOptionsDialog by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showOptionsDialog = true },
                        modifier = Modifier.size(22.pixelDp)
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_three_dots,
                            contentDescription = "Layer Options",
                            tint = AppTheme.colors.TextColorLight,
                        )
                    }

                    if (showOptionsDialog) {
                        LayerOptionsDialog(
                            layer = layer,
                            layerImage = bitmapImage,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            canMergeDown = true,
                            onBlendModeSelected = { mode ->
                                onEvent(
                                    LayerEvent.SetBlendMode(
                                        layer.id,
                                        mode
                                    )
                                )
                            },
                            onMergeDown = { onEvent(LayerEvent.MergeLayerDown(layer.id)) },
                            onDismiss = { showOptionsDialog = false }
                        )
                    }

                }
            }
        }

        // Opacity slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.pixelDp)
        ) {
            Text(
                text = "${(localOpacity * 100).toInt()}%",
                color = AppTheme.colors.TextColorLight,
                fontSize = 11.sp,
                modifier = Modifier.width(24.pixelDp)
            )
            Slider(
                value = localOpacity,
                onValueChange = { localOpacity = it },
                onValueChangeFinished = {
                    onEvent(LayerEvent.SetLayerOpacity(layer.id, localOpacity))
                },
                valueRange = 0f..1f,
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
                    .height(16.pixelDp)
            )
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
                        AppTheme.colors.InfoColor.toArgb()
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
                        AppTheme.colors.ErrorDarkColor.toArgb()
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