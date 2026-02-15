package com.olaz.instasprite.ui.components.composable

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

@Composable
fun ColorPaletteList(
    palettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier,
    optionSlot: @Composable (ColorPalette) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .background(CatppuccinUI.BackgroundColorDarker),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(palettes) { palette ->
            ListEntry(
                palette = palette,
                optionSlot = optionSlot,
                onClick = { onPaletteSelected(palette) }
            )
        }
    }
}

@Composable
private fun ListEntry(
    palette: ColorPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    optionSlot: @Composable (ColorPalette) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CatppuccinUI.BackgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = palette.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = CatppuccinUI.TextColorLight
                )
                Text(
                    text = "by ${palette.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatppuccinUI.Subtext0Color
                )
            }

            optionSlot(palette)
        }

        Spacer(modifier = Modifier.height(8.dp))

        ColorPaletteView(
            colors = palette.colors,
            config = ColorPaletteConfig(
                itemSpacing = 4.dp,
                listHeight = 32.dp,
                colorItemSize = 24.dp,
                isInteractive = false
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun PreviewColorPaletteList() {
    InstaSpriteTheme {
        val dummyPalettes = listOf(
            ColorPalette(
                id = 1,
                colors = mutableListOf(
                    Color(0xFF0077BE),
                    Color(0xFF0096C7),
                    Color(0xFF48CAE4),
                    Color(0xFF90E0EF),
                    Color(0xFFCAF0F8)
                )
            ),
            ColorPalette(
                id = 2,
                colors = mutableListOf(
                    Color(0xFF355070),
                    Color(0xFF6D597A),
                    Color(0xFFB56576),
                    Color(0xFFE56B6F),
                    Color(0xFFEAAC8B)
                )
            ),
            ColorPalette(
                id = 3,
                colors = mutableListOf(
                    Color(0xFF2D6A4F),
                    Color(0xFF40916C),
                    Color(0xFF52B788),
                    Color(0xFF74C69D),
                    Color(0xFF95D5B2)
                )
            )
        )

        ColorPaletteList(
            palettes = dummyPalettes,
            onPaletteSelected = {}
        )
    }
}

@Preview
@Composable
private fun PreviewColorPaletteListWithOption() {
    InstaSpriteTheme {
        val dummyPalettes = listOf(
            ColorPalette(
                id = 1,
                colors = mutableListOf(
                    Color(0xFF0077BE),
                    Color(0xFF0096C7),
                    Color(0xFF48CAE4),
                    Color(0xFF90E0EF),
                    Color(0xFFCAF0F8)
                )
            ),
            ColorPalette(
                id = 2,
                colors = mutableListOf(
                    Color(0xFF355070),
                    Color(0xFF6D597A),
                    Color(0xFFB56576),
                    Color(0xFFE56B6F),
                    Color(0xFFEAAC8B)
                )
            ),
            ColorPalette(
                id = 3,
                colors = mutableListOf(
                    Color(0xFF2D6A4F),
                    Color(0xFF40916C),
                    Color(0xFF52B788),
                    Color(0xFF74C69D),
                    Color(0xFF95D5B2)
                )
            )
        )

        ColorPaletteList(
            palettes = dummyPalettes,
            onPaletteSelected = {},
            optionSlot = { palette ->
                Button(
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.CurrentPalette.Peach
                    ),
                ) {
                    Text(text = "Test", color = CatppuccinUI.TextColorDark)
                }
            }
        )
    }
}