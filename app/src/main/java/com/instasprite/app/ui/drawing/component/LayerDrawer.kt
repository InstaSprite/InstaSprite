package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.canMergeLayerDown
import com.instasprite.app.ui.components.composable.BackButton
import com.instasprite.app.ui.components.composable.Bar
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LayerDrawer(
    layers: List<Layer>,
    activeLayerId: String,
    canvasWidth: Int,
    canvasHeight: Int,
    onEvent: (LayerEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    animation: Animation? = null
) {

    val lazyListState = rememberLazyListState()

    var layersList by remember(layers) { mutableStateOf(layers) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        layersList = layersList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    var previousSize by remember { mutableIntStateOf(layers.size) }

    LaunchedEffect(layers.size) {
        if (layers.size > previousSize) {
            lazyListState.animateScrollToItem(0)
        }
        previousSize = layers.size
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(AppTheme.colors.surfaceInset)
            .padding(8.pixelDp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.pixelDp)
        ) {
            Text(
                text = stringResource(R.string.layers),
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )

        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(layersList, key = { it.id }) { layer ->

                val dragIndicatorBgColor = if (layer.id == activeLayerId) {
                    AppTheme.colors.blue
                } else {
                    AppTheme.colors.background
                }

                val dragIndicatorColor = if (layer.id == activeLayerId) {
                    AppTheme.colors.onAccent
                } else {
                    AppTheme.colors.onSurface
                }

                ReorderableItem(
                    state = reorderableLazyListState,
                    key = layer.id
                ) { isDragging ->

                    LaunchedEffect(isDragging) {
                        if (!isDragging) {
                            val currentIndex = layersList.indexOf(layer)
                            val originalIndex = layers.indexOf(layer)
                            if (currentIndex != -1 && originalIndex != -1 && currentIndex != originalIndex) {
                                onEvent(
                                    LayerEvent.ReorderLayer(
                                        (layers.size - 1) - originalIndex,
                                        (layers.size - 1) - currentIndex
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(dragIndicatorBgColor)
                    ) {
                        IconButton(
                            modifier = Modifier
                                .draggableHandle()
                                .width(22.pixelDp),
                            onClick = {},
                        ) {
                            PixelIcon(
                                icon = R.drawable.ic_grab_handle,
                                tint = dragIndicatorColor,
                                contentDescription = stringResource(R.string.reorder)
                            )
                        }

                        LayerView(
                            layer = layer,
                            onEvent = onEvent,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            isActive = layer.id == activeLayerId,
                            onClick = { onEvent(LayerEvent.SelectLayer(layer.id)) },
                            onVisibilityToggle = { onEvent(LayerEvent.ToggleVisibility(layer.id)) },
                            onLockToggle = { onEvent(LayerEvent.ToggleLock(layer.id)) },
                            onDelete = { onEvent(LayerEvent.DeleteLayer(layer.id)) },
                            canClip = layers.indexOfFirst { it.id == layer.id } < layers.lastIndex,
                            canMergeDown = layers.asReversed()
                                .canMergeLayerDown(layer.id, animation)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.pixelDp))
        HorizontalDivider(color = AppTheme.colors.purple)

        Bar(
            leftSlot = {
                BackButton(
                    onClick = onBack,
                    color = AppTheme.colors.onSurface
                )
            },
            rightSlot = {
                Button(
                    onClick = { onEvent(LayerEvent.AddLayer) },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.green
                    )
                ) {
                    PixelIcon(
                        icon = R.drawable.ic_plus,
                        contentDescription = stringResource(R.string.add_layer),
                        tint = AppTheme.colors.onAccent
                    )
                }
            },
            backgroundColor = AppTheme.colors.surfaceInset
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun Preview() {
    InstaSpriteTheme {
        LayerDrawer(
            layers = listOf(
                Layer(
                    id = "test1",
                    name = "Layer 1",
                    cel = Cel(
                        x = 0,
                        y = 0,
                        width = 16,
                        height = 16,
                        pixels = IntArray(16 * 16) {
                            AppTheme.colors.mutedRed.toArgb()
                        }
                    )
                ),
                Layer(
                    id = "test2",
                    name = "Layer 2",
                    cel = Cel(
                        x = 0,
                        y = 0,
                        width = 16,
                        height = 16,
                        pixels = IntArray(16 * 16) {
                            AppTheme.colors.purple.toArgb()
                        }
                    )
                )
            ),
            activeLayerId = "test2",
            canvasWidth = 16,
            canvasHeight = 16,
            onEvent = {}
        )
    }
}