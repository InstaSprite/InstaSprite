package com.instasprite.app.ui.drawing.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.domain.tool.DitherBrush
import com.instasprite.app.domain.tool.DitherPattern
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.drawing.contract.ToolSelectorEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp

@Composable
fun DitherOptions(
    brush: DitherBrush,
    foreground: Color,
    palette: List<Color>,
    onEvent: (ToolSelectorEvent) -> Unit
) {
    var patternsExpanded by remember { mutableStateOf(false) }
    val patternListState = rememberLazyListState()
    val colors = AppTheme.colors
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = colors.BackgroundColorDarker, contentColor = colors.TextColorLight
    )
    val description = stringResource(R.string.dither_pattern)
    Box {
        Button(
            onClick = { patternsExpanded = true },
            modifier = Modifier
                .height(24.pixelDp)
                .semantics { contentDescription = description },
            colors = buttonColors,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 6.pixelDp, vertical = 1.pixelDp)
        ) {
            Text(stringResource(R.string.dither_pattern_label), fontSize = 12.sp)
            Spacer(Modifier.width(6.pixelDp))
            DitherPatternPreview(brush.pattern, foreground, Color(brush.secondaryColor))
        }
        DropdownMenu(
            expanded = patternsExpanded, onDismissRequest = { patternsExpanded = false },
            modifier = Modifier
                .width(200.pixelDp)
                .background(colors.BackgroundColor)
        ) {
            DitherMenuContent(brush, foreground, palette, onEvent, patternListState)
        }
    }
}

@Composable
private fun DitherMenuContent(
    brush: DitherBrush,
    foreground: Color,
    palette: List<Color>,
    onEvent: (ToolSelectorEvent) -> Unit,
    patternListState: LazyListState
) {
    val colors = AppTheme.colors
    Column(Modifier.padding(6.pixelDp)) {
        Row(
            modifier = Modifier.width(188.pixelDp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dither_background),
                color = colors.TextColorLight,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )

        }

        Row {
            IconButton(
                onClick = { onEvent(ToolSelectorEvent.OpenDitherColorWheel) },
                modifier = Modifier.size(24.pixelDp)
            ) {
                PixelIcon(
                    R.drawable.ic_plus,
                    contentDescription = stringResource(R.string.add_color)
                )
            }
            ColorPaletteView(
                colors = (listOf(Color.Transparent) + palette).distinct(),
                activeColor = Color(brush.secondaryColor),
                modifier = Modifier.width(188.pixelDp),
                onColorSelected = { onEvent(ToolSelectorEvent.SetDitherBackground(it)) }
            )
        }

    }
    LazyRow(
        state = patternListState,
        modifier = Modifier
            .width(200.pixelDp)
            .height(38.pixelDp),
        contentPadding = PaddingValues(6.pixelDp),
        horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(DitherPattern.entries, key = { it.name }) { pattern ->
            val description = stringResource(pattern.nameRes)
            Box(
                modifier = Modifier
                    .size(26.pixelDp)
                    .background(colors.BackgroundColorDarker)
                    .border(
                        1.pixelDp,
                        if (brush.pattern == pattern) colors.AccentButtonColor else colors.Subtext0Color,
                    )
                    .semantics { contentDescription = description }
                    .selectable(
                        selected = brush.pattern == pattern, role = Role.RadioButton,
                        onClick = { onEvent(ToolSelectorEvent.SetDitherPattern(pattern)) }),
                contentAlignment = Alignment.Center
            ) {
                DitherPatternPreview(
                    pattern,
                    foreground,
                    Color(brush.secondaryColor),
                    Modifier.size(24.pixelDp)
                )
            }
        }
    }
}

@Composable
private fun DitherPatternPreview(
    pattern: DitherPattern, foreground: Color, background: Color,
    modifier: Modifier = Modifier.size(16.pixelDp)
) {
    val bitmap = remember(pattern, foreground, background) {
        val brush = DitherBrush(pattern, background.toArgb())
        val pixels = IntArray(64) { brush.colorAt(it / 8, it % 8, foreground.toArgb()) }
        Bitmap.createBitmap(pixels, 8, 8, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    Image(
        bitmap = bitmap, contentDescription = null, filterQuality = FilterQuality.None,
        modifier = modifier.drawCheckerboard(8, 8)
    )
}

@Preview(name = "Dither shading patterns", showBackground = true)
@Composable
private fun DitherOptionsPreview() {
    InstaSpriteTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(4.pixelDp)) {
            DitherPattern.entries.forEach { DitherPatternPreview(it, Color.Black, Color.White) }
        }
    }
}

@Preview(name = "Dither controls", showBackground = true)
@Composable
private fun DitherControlsPreview() {
    InstaSpriteTheme {
        DitherOptions(
            DitherBrush(DitherPattern.Shade50),
            Color.Black,
            listOf(Color.White, Color.Red),
            {})
    }
}

@Preview(name = "Dither menu", showBackground = true)
@Composable
private fun DitherMenuPreview() {
    InstaSpriteTheme {
        Column(Modifier
            .width(200.pixelDp)
            .background(AppTheme.colors.BackgroundColor)) {
            DitherMenuContent(
                DitherBrush(DitherPattern.Shade50), Color.Black,
                listOf(Color.White, Color.Red, Color.Blue), {}, rememberLazyListState()
            )
        }
    }
}
