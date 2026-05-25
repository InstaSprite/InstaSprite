package com.instasprite.app.ui.components.composable

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp

@Immutable
data class ColorPaletteConfig(
    val backgroundColor: Color = Color.Unspecified,
    val itemSpacing: Dp = 0.pixelDp,
    val listHeight: Dp = 26.pixelDp,
    val colorItemSize: Dp = 22.pixelDp,
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
        val resolvedBg =
            if (backgroundColor == Color.Unspecified) AppTheme.colors.BackgroundColorDarker else backgroundColor
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
fun PalettePreview(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    lines: Int = 1,
    config: ColorPaletteConfig = ColorPaletteConfig.Default
) {
    val colorCount = colors.size
    if (colorCount == 0) return

    val resolvedBg = if (config.backgroundColor == Color.Unspecified) {
        AppTheme.colors.BackgroundColorDarker
    } else {
        config.backgroundColor
    }

    if (lines <= 1) {
        SingleLinePalettePreview(
            colors = colors,
            colorCount = colorCount,
            resolvedBg = resolvedBg,
            config = config,
            modifier = modifier
        )
    } else {
        val minColorsPerLine = 8
        val effectiveLines = (colorCount / minColorsPerLine).coerceIn(1, lines)
        MultiLinePalettePreview(
            colors = colors,
            colorCount = colorCount,
            lines = effectiveLines,
            resolvedBg = resolvedBg,
            config = config,
            modifier = modifier
        )
    }
}

@Composable
private fun SingleLinePalettePreview(
    colors: List<Color>,
    colorCount: Int,
    resolvedBg: Color,
    config: ColorPaletteConfig,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val paletteBitmap = remember(colors, config.colorItemSize, config.itemSpacing) {
        val itemPx = with(density) { config.colorItemSize.toPx() }
        val spacingPx = with(density) { config.itemSpacing.toPx() }
        val checkerPx = with(density) { 4.dp.toPx() }
        val totalW = (itemPx * colorCount + spacingPx * (colorCount - 1).coerceAtLeast(0))
            .toInt().coerceAtLeast(1)
        val h = itemPx.toInt().coerceAtLeast(1)

        renderPaletteBitmap(colors, totalW, h, itemPx, spacingPx, checkerPx) { index, itemPx, spacingPx ->
            val x = index * (itemPx + spacingPx)
            Offset(x, 0f)
        }
    }

    val contentPadding = (config.listHeight - config.colorItemSize) / 2

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .height(config.listHeight)
            .background(resolvedBg)
            .clipToBounds()
    ) {
        Image(
            bitmap = paletteBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .padding(horizontal = contentPadding)
                .height(config.colorItemSize)
                .horizontalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun MultiLinePalettePreview(
    colors: List<Color>,
    colorCount: Int,
    lines: Int,
    resolvedBg: Color,
    config: ColorPaletteConfig,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val colorsPerLine = kotlin.math.ceil(colorCount.toFloat() / lines).toInt()
    val effectiveColorsPerLine = maxOf(colorsPerLine, 10)
    val contentPadding = (config.listHeight - config.colorItemSize) / 2

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(resolvedBg)
            .clipToBounds()
            .padding(contentPadding)
    ) {
        val availableWidthPx = with(density) { maxWidth.toPx() }

        val spacingPx = with(density) { config.itemSpacing.toPx() }
        val itemPx = (availableWidthPx - spacingPx * (effectiveColorsPerLine - 1).coerceAtLeast(0)) / effectiveColorsPerLine
        val checkerPx = with(density) { 4.pixelDp.toPx() }

        val totalW = availableWidthPx.toInt().coerceAtLeast(1)
        val totalH = (itemPx * lines + spacingPx * (lines - 1).coerceAtLeast(0))
            .toInt().coerceAtLeast(1)

        val paletteBitmap = remember(colors, lines, totalW, totalH, effectiveColorsPerLine) {
            renderPaletteBitmap(colors, totalW, totalH, itemPx, spacingPx, checkerPx) { index, itemPx, spacingPx ->
                val col = index % effectiveColorsPerLine
                val row = index / effectiveColorsPerLine
                val x = col * (itemPx + spacingPx)
                val y = row * (itemPx + spacingPx)
                Offset(x, y)
            }
        }

        val totalHeight = with(density) {
            (itemPx * lines + spacingPx * (lines - 1).coerceAtLeast(0)).toDp()
        }

        Image(
            bitmap = paletteBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        )
    }
}

private fun renderPaletteBitmap(
    colors: List<Color>,
    width: Int,
    height: Int,
    itemPx: Float,
    spacingPx: Float,
    checkerPx: Float,
    positionForIndex: (index: Int, itemPx: Float, spacingPx: Float) -> Offset
): ImageBitmap {
    val bmp = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    colors.forEachIndexed { index, color ->
        val pos = positionForIndex(index, itemPx, spacingPx)
        val x = pos.x
        val y = pos.y

        if (color.alpha < 1f) {
            val cols = (itemPx / checkerPx).toInt() + 1
            val rows = (itemPx / checkerPx).toInt() + 1
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    paint.color = if ((row + col) % 2 == 0) 0xFFAAAAAA.toInt() else 0xFFFFFFFF.toInt()
                    canvas.drawRect(
                        x + col * checkerPx,
                        y + row * checkerPx,
                        (x + (col + 1) * checkerPx).coerceAtMost(x + itemPx),
                        (y + (row + 1) * checkerPx).coerceAtMost(y + itemPx),
                        paint
                    )
                }
            }
        }

        paint.color = color.toArgb()
        canvas.drawRect(x, y, x + itemPx, y + itemPx, paint)
    }

    return bmp.asImageBitmap()
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
@Preview
@Composable
private fun PreviewPreview() {
    InstaSpriteTheme {
        PalettePreview(
            colors = listOf(
                Color.White,
                Color.Green,
                Color.Blue,
                Color.Yellow,
                Color.Magenta,
                Color.Cyan,
            ),
            config = ColorPaletteConfig(
                backgroundColor = Color.Red,
                itemSpacing = 0.pixelDp,
                listHeight = 26.pixelDp,
                colorItemSize = 22.pixelDp,
                isInteractive = true,
            )
        )
    }
}