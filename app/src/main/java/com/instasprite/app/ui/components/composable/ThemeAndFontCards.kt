package com.instasprite.app.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppColors
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.buildCatppuccinTypography
import com.instasprite.app.utils.pixelDp

@Composable
fun FlavourCard(
    label: String,
    previewColors: AppColors,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val borderColor = if (isSelected) colors.SelectedColor else colors.Foreground1Color

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.pixelDp, borderColor, MaterialTheme.shapes.small)
            .background(previewColors.BackgroundColor)
            .clickable(onClick = onClick)
            .padding(6.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swatch row — key semantic colors from the theme
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
            modifier = Modifier.padding(vertical = 2.pixelDp)
        ) {
            listOf(
                previewColors.SelectedColor,
                previewColors.AccentButtonColor,
                previewColors.DismissButtonColor,
                previewColors.LinkColor,
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(6.pixelDp)
                        .clip(PixelShape())
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.pixelDp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = previewColors.TextColorLight,
        )
    }
}

@Composable
fun FontCard(
    font: AppFont,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val borderColor = if (isSelected) colors.SelectedColor else colors.Foreground1Color

    val typography = buildCatppuccinTypography(colors, font)

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.pixelDp, borderColor, MaterialTheme.shapes.small)
            .background(colors.BackgroundColorDarker)
            .clickable(onClick = onClick)
            .padding(6.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aa",
            style = typography.titleLarge,
            color = if (isSelected) colors.SelectedColor else colors.TextColorLight,
        )

        Spacer(modifier = Modifier.height(4.pixelDp))

        Text(
            text = font.label,
            style = typography.bodyMedium,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = colors.TextColorLight,
        )
    }
}
