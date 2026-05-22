package com.instasprite.app.ui.components.composable

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData

@Composable
fun ColorPaletteList(
    palettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    colorPaletteConfig: ColorPaletteConfig = ColorPaletteConfig(isInteractive = false),
    optionSlot: @Composable (ColorPalette) -> Unit = {}
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .background(AppTheme.colors.BackgroundColorDarker),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = palettes,
            key = { palette ->
                if (palette.id == -1) "default_${palette.name}" else "saved_${palette.id}"
            }
        ) { palette ->
            ListEntry(
                palette = palette,
                optionSlot = optionSlot,
                colorPaletteConfig = colorPaletteConfig,
                onClick = { onPaletteSelected(palette) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ListEntry(
    palette: ColorPalette,
    onClick: () -> Unit,
    colorPaletteConfig: ColorPaletteConfig,
    modifier: Modifier = Modifier,
    optionSlot: @Composable (ColorPalette) -> Unit = {}
) {
    val itemModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(AppTheme.colors.BackgroundColor)
        .clickable(onClick = onClick)
        .padding(12.dp)

    Column(modifier = itemModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = palette.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextColorLight
                )
                Text(
                    text = "by ${palette.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.Subtext0Color
                )
            }

            optionSlot(palette)
        }

        Spacer(modifier = Modifier.height(8.dp))

        ColorPaletteView(
            colors = palette.colors,
            config = colorPaletteConfig,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun PreviewColorPaletteList() {
    InstaSpriteTheme {
        ColorPaletteList(
            palettes = DummyData.palettes,
            onPaletteSelected = {}
        )
    }
}

@Preview
@Composable
private fun PreviewColorPaletteListWithOption() {
    InstaSpriteTheme {

        ColorPaletteList(
            palettes = DummyData.palettes,
            onPaletteSelected = {},
            optionSlot = { palette ->
                Button(
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.WarningColor
                    ),
                ) {
                    Text(text = stringResource(R.string.test), color = AppTheme.colors.TextColorDark)
                }
            }
        )
    }
}