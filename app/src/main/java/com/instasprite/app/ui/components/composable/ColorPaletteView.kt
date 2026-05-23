package com.instasprite.app.ui.components.composable

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

import com.instasprite.app.utils.drawCheckerboard

@Immutable
data class ColorPaletteConfig(
    val backgroundColor: Color = Color.Unspecified,
    val itemSpacing: Dp = 0.pixelDp,
    val listHeight: Dp = 26.pixelDp,
    val colorItemSize: Dp = 22.pixelDp,
    val isInteractive: Boolean = true,
    val isWrap: Boolean = false,
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
        val resolvedBg =
            if (backgroundColor == Color.Unspecified) AppTheme.colors.BackgroundColorDarker else backgroundColor
        BoxWithConstraints(
            contentAlignment = Alignment.CenterStart,
            modifier = modifier
                .then(if (isWrap) Modifier.fillMaxWidth() else Modifier.height(height = listHeight))
                .background(resolvedBg)
        ) {
            val effectiveColorItemSize = if (isWrap && colors.isNotEmpty()) {
                val wAvailPx = maxOf(0f, (maxWidth - (listHeight - colorItemSize)).value)
                val dPx = colorItemSize.value
                val sPx = itemSpacing.value
                val n = if (dPx + sPx > 0) {
                    Math.floor(((wAvailPx + sPx) / (dPx + sPx)).toDouble()).toInt()
                } else {
                    1
                }
                val lines = if (n > 0) Math.ceil(colors.size.toDouble() / n).toInt() else 1
                if (lines >= 3) {
                    val nNew = Math.ceil(colors.size.toDouble() / 2.0).toInt()
                    if (nNew > 0) {
                        val calculatedSize = (wAvailPx + sPx) / nNew - sPx
                        maxOf(12f, calculatedSize).dp
                    } else {
                        colorItemSize
                    }
                } else {
                    colorItemSize
                }
            } else {
                colorItemSize
            }

            if (isWrap) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (listHeight - effectiveColorItemSize) / 2, vertical = (listHeight - effectiveColorItemSize) / 2)
                ) {
                    colors.forEach { color ->
                        val modifier = (itemColorModifier ?: Modifier).size(effectiveColorItemSize)

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
            } else {
                if (isInteractive) {
                    LazyRow(
                        state = lazyListState,
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        modifier = Modifier.padding(horizontal = (listHeight - colorItemSize) / 2)
                    ) {
                        items(colors) { color ->
                            val modifier = (itemColorModifier ?: Modifier).size(colorItemSize)
                            if (color == activeColor) {
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
                } else {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = (listHeight - colorItemSize) / 2)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing)
                    ) {
                        colors.forEach { color ->
                            val modifier = (itemColorModifier ?: Modifier).size(colorItemSize)
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
}


@Composable
fun ColorItem(
    color: Color,
    onColorSelected: ((Color) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .then(if (color.alpha < 1f) Modifier.drawCheckerboard() else Modifier)
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
            .then(if (color.alpha < 1f) Modifier.drawCheckerboard() else Modifier)
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
    InstaSpriteTheme {
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
                backgroundColor = Color.Red,
                itemSpacing = 0.pixelDp,
                listHeight = 26.pixelDp,
                colorItemSize = 22.pixelDp,
                isInteractive = true,
            ),
            onColorSelected = {},
        )
    }
}