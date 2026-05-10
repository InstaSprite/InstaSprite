package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.AppTheme

@Immutable
data class ColorPaletteConfig(
    val backgroundColor: Color = Color.Unspecified,
    val itemSpacing: Dp = 0.dp,
    val listHeight: Dp = 40.dp,
    val colorItemSize: Dp = 30.dp,
    val isInteractive: Boolean = true,
) {
    init {
        require(listHeight >= colorItemSize) {
            "listHeight ($listHeight) must be greater than or equal to colorItemSize ($colorItemSize)"
        }
    }

    companion object {
        val Default = ColorPaletteConfig()
    }
}

@Composable
fun ColorPaletteView(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
    onColorSelected: ((Color) -> Unit)? = null,
    config: ColorPaletteConfig = ColorPaletteConfig.Default,
    lazyListState: LazyListState = rememberLazyListState(),
    itemColorModifier: Modifier? = null,
) {
    with(config) {
        val resolvedBg = if (backgroundColor == Color.Unspecified) AppTheme.colors.BackgroundColorDarker else backgroundColor
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = modifier
                .height(height = listHeight)
                .fillMaxWidth()
                .background(resolvedBg),
        ) {
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier.padding(horizontal = (listHeight - colorItemSize) / 2)
            ) {
                items(colors) { color ->

                    val modifier = (itemColorModifier ?: Modifier).size(colorItemSize)

                    if (isInteractive && color == activeColor) {
                        ActiveColorItem(
                            color = color,
                            modifier = modifier,
                            onClick = { onColorSelected?.invoke(color) }
                        )
                    } else {
                        ColorItem(
                            color = color,
                            onColorSelected = onColorSelected,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    onColorSelected: ((Color) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color)
            .then(
                if (onColorSelected != null) {
                    Modifier.clickable { onColorSelected(color) }
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun ActiveColorItem(
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val luminance = color.luminance()
    val indicatorColor = if (luminance < 0.4f) Color.White else Color.Black

    Box(
        modifier = modifier
            .background(color)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val trianglePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.4f, 0f)
                lineTo(0f, size.height * 0.4f)
                close()
            }
            drawPath(trianglePath, color = indicatorColor)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ColorPaletteView(
        colors = listOf(
            Color.White,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
            Color.Cyan,
        ),
        activeColor = Color.White,
        config = ColorPaletteConfig(
            itemSpacing = 0.dp,
            listHeight = 40.dp,
            colorItemSize = 30.dp,
            isInteractive = true,
        ),
        onColorSelected = {},
    )
}