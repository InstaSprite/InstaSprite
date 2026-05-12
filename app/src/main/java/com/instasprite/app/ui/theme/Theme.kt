package com.instasprite.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun InstaSpriteTheme(
    flavour: ThemeFlavour = ThemeFlavour.MOCHA,
    content: @Composable () -> Unit
) {
    val palette = Catppuccin.fromFlavour(flavour)
    val appColors = Catppuccin.toAppColors(palette)
    val typography = buildCatppuccinTypography(appColors)

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


    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}