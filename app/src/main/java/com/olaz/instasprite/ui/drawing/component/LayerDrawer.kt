package com.olaz.instasprite.ui.drawing.component

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.domain.model.Cel
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.ui.components.composable.BackButton
import com.olaz.instasprite.ui.components.composable.Bar
import com.olaz.instasprite.ui.drawing.contract.LayerEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
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
    onBack: () -> Unit = {}
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
            .background(CatppuccinUI.BackgroundColorDarker)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Layers",
                color = CatppuccinUI.TextColorLight,
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

                val dragIndicatorBgColor = if (layer.id == activeLayerId ){
                    CatppuccinUI.CurrentPalette.Blue
                } else {
                    CatppuccinUI.BackgroundColor
                }

                val dragIndicatorColor = if (layer.id == activeLayerId ){
                    CatppuccinUI.TextColorDark
                } else {
                    CatppuccinUI.TextColorLight
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
                                onEvent(LayerEvent.ReorderLayer((layers.size - 1) - originalIndex, (layers.size - 1) - currentIndex))
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
                                .width(32.dp),
                            onClick = {},
                        ) {
                            Icon(
                                Icons.Rounded.DragHandle,
                                tint = dragIndicatorColor,
                                contentDescription = "Reorder"
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
                            onDelete = { onEvent(LayerEvent.DeleteLayer(layer.id)) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = CatppuccinUI.SelectedColor)

        Bar(
            leftSlot = {
                BackButton(
                    onClick = onBack,
                    color = CatppuccinUI.TextColorLight
                )
            },
            rightSlot = {
                Button(
                    onClick = { onEvent(LayerEvent.AddLayer) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.AccentButtonColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Layer",
                        tint = CatppuccinUI.TextColorDark
                    )
                }
            },
            backgroundColor = CatppuccinUI.BackgroundColorDarker
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun Preview() {
    InstaSpriteTheme() {
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
                            CatppuccinUI.CurrentPalette.Maroon.toArgb()
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
                            CatppuccinUI.CurrentPalette.Mauve.toArgb()
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