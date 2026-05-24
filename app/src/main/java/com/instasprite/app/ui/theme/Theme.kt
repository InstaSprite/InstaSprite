package com.instasprite.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.instasprite.app.ui.components.shape.PixelShape

@Composable
fun InstaSpriteTheme(
    flavour: ThemeFlavour = ThemeFlavour.MOCHA,
    appFont: AppFont = AppFont.DETERMINATION,
    content: @Composable () -> Unit
) {
    val palette = Catppuccin.fromFlavour(flavour)
    val appColors = Catppuccin.toAppColors(palette)
    val typography = buildCatppuccinTypography(appColors, appFont)

    val colorScheme = darkColorScheme(
        primary = appColors.SelectedColor,
        secondary = appColors.AccentButtonColor,
        tertiary = appColors.SecondaryAccentColor,
        background = appColors.BackgroundColor,
        surface = appColors.Foreground0Color,
        onPrimary = appColors.TextColorDark,
        onSecondary = appColors.TextColorDark,
        onBackground = appColors.TextColorLight,
        onSurface = appColors.TextColorLight,
    )

    val shapes = Shapes(
        extraSmall = PixelShape(),
        small = PixelShape(),
        medium = PixelShape(2),
        large = PixelShape(2),
        extraLarge = PixelShape(2),
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}