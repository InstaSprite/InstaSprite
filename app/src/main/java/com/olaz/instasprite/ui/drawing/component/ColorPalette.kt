package com.olaz.instasprite.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.drawing.contract.CanvasMenuEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteEvent
import com.olaz.instasprite.ui.drawing.contract.ColorPaletteState
import com.olaz.instasprite.ui.theme.AppTheme
import com.olaz.instasprite.utils.toHexString
import kotlinx.coroutines.launch

@Composable
fun ColorPalette(
    modifier: Modifier = Modifier,
    colorPaletteState: ColorPaletteState,
    onColorPaletteEvent: (ColorPaletteEvent) -> Unit,
    onCanvasMenuEvent: (CanvasMenuEvent) -> Unit
) {

    val colorPalette = colorPaletteState.colorPalette
    val activeColor = colorPaletteState.activeColor
    val recentColors = colorPaletteState.recentColors

    val colorPaletteListState = rememberLazyListState()
    val recentColorsListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    LaunchedEffect(activeColor) {
        if (activeColor in colorPalette) {
            val index = colorPalette.indexOf(activeColor)
            val visibleIndices = colorPaletteListState.layoutInfo.visibleItemsInfo.map { it.index }

            if (index !in visibleIndices) {
                coroutineScope.launch {
                    colorPaletteListState.scrollItemToCenter(
                        index = index,
                        itemSizeDp = 40.dp,
                        itemSpacingDp = 6.dp,
                        density = density
                    )
                }
            }
        }
    }

    LaunchedEffect(recentColors.firstOrNull()) {
        coroutineScope.launch {
            recentColorsListState.animateScrollToItem(0)
        }
    }

    var showCanvasMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
    ) {
        ColorPaletteView(
            colors = colorPalette,
            activeColor = activeColor,
            onColorSelected = { color ->
                onColorPaletteEvent(ColorPaletteEvent.SelectColor(color))
            },
            lazyListState = colorPaletteListState,
        )

        // ColorPaletteContent already has 4.dp padding; add 2.dp for consistency
        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActiveColor(
                activeColor = activeColor,
                onClick = {
                    if (activeColor in colorPalette) {
                        val index = colorPalette.indexOf(activeColor)
                        coroutineScope.launch {
                            colorPaletteListState.scrollItemToCenter(
                                index = index,
                                itemSizeDp = 30.dp,
                                itemSpacingDp = 0.dp,
                                density = density
                            )
                        }
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .width(86.dp) //  ̶E̶q̶u̶a̶l̶ ̶t̶w̶o̶ ̶c̶o̶l̶o̶r̶ ̶i̶t̶e̶m̶ ̶i̶n̶ ̶p̶a̶l̶e̶t̶t̶e̶ ̶+̶ ̶s̶p̶a̶c̶i̶n̶g̶
            )

            IconButton(
                onClick = { onColorPaletteEvent(ColorPaletteEvent.OpenColorWheelDialog) },
                modifier = Modifier
                    .size(45.dp)
                    .padding(horizontal = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Show color wheel",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Recent Colors section
            ColorPaletteView(
                colors = recentColors.toList(),
                config = ColorPaletteConfig(listHeight = 40.dp),
                onColorSelected = { color ->
                    onColorPaletteEvent(ColorPaletteEvent.SelectColor(color))
                },
                lazyListState = recentColorsListState,
                modifier = Modifier
                    .weight(1f)
            )

            // Opt button: show a TODO: dialog with canvas option
            Box {
                IconButton(
                    onClick = { showCanvasMenu = true },
                    modifier = Modifier
                        .size(45.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Show opts",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                CanvasMenuDropdownMenu(
                    expanded = showCanvasMenu,
                    onEvent = onCanvasMenuEvent,
                    onDismiss = { showCanvasMenu = false },
                )

            }

        }
    }
}

@Composable
private fun ActiveColor(
    activeColor: Color,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    val textColor = if (activeColor.luminance() < 0.4f) Color.White else Color.Black

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(activeColor)
            .border(
                width = 5.dp,
                color = AppTheme.colors.BackgroundColorDarker
            )
            .clickable(
                onClick = onClick
            )
    ) {
        Text(
            text = activeColor.toHexString(),
            color = textColor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CanvasMenuDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEvent: (CanvasMenuEvent) -> Unit,
    modifier: Modifier = Modifier,
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(AppTheme.colors.DropDownMenuColor)
    ) {
        DropdownMenuItem(
            text = {
                Text(text = "Rotate Canvas")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_redo),
                    contentDescription = "Rotate Canvas",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            onClick = { onEvent(CanvasMenuEvent.RotateCanvas) }
        )

        DropdownMenuItem(
            text = {
                Text(text = "Horizontal Flip")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_hflip),
                    contentDescription = "Flip canvas horizontal",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            onClick = { onEvent(CanvasMenuEvent.HorizontalFlip) }
        )

        DropdownMenuItem(
            text = {
                Text(text = "Vertical Flip")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_vflip),
                    contentDescription = "Flip canvas vertical",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            onClick = { onEvent(CanvasMenuEvent.VerticalFlip) }
        )

        DropdownMenuItem(
            text = {
                Text(text = "Resize")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_resize),
                    contentDescription = "Resize canvas",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            onClick = { onEvent(CanvasMenuEvent.OpenResizeDialog) }
        )
    }
}


private suspend fun LazyListState.scrollItemToCenter(
    index: Int,
    itemSizeDp: Dp,
    itemSpacingDp: Dp,
    density: Density
) {
    val itemSizeWithSpacing = itemSizeDp + itemSpacingDp
    val itemSizePx = with(density) { itemSizeWithSpacing.toPx().toInt() }

    val centerOffset = -((layoutInfo.viewportEndOffset - itemSizePx) / 2)
    animateScrollToItem(index, centerOffset)
}
