package com.instasprite.app.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.pixelDp


@Composable
fun PaletteListEntry(
    palette: ColorPalette,
    onClick: () -> Unit,
    colorPaletteConfig: ColorPaletteConfig,
    modifier: Modifier = Modifier,
    optionSlot: @Composable (ColorPalette) -> Unit = {}
) {
    val itemModifier = modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(AppTheme.colors.BackgroundColor)
        .clickable(onClick = onClick)
        .padding(8.pixelDp)

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

        Spacer(modifier = Modifier.height(6.pixelDp))

        PalettePreview(
            colors = palette.colors,
            config = colorPaletteConfig,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        PaletteListEntry(
            palette = DummyData.palettes[0],
            onClick = {},
            colorPaletteConfig = ColorPaletteConfig(isInteractive = false)
        )
    }
}