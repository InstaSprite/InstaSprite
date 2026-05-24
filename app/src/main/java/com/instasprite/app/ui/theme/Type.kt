package com.instasprite.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.instasprite.app.R
import kotlinx.serialization.Serializable


@Serializable
enum class AppFont(val label: String) {
    DETERMINATION("Determination"),
    RETRON("Retron"),
    SYSTEM("System")
}

val RetronFont = FontFamily(
    Font(R.font.svn_retron, FontWeight.Normal)
)

val DeterminationFont = FontFamily(
    Font(R.font.svn_determination, FontWeight.Normal)
)

fun buildCatppuccinTypography(colors: AppColors, appFont: AppFont = AppFont.DETERMINATION): Typography {
    val selectedFont = when (appFont) {
        AppFont.DETERMINATION -> DeterminationFont
        AppFont.RETRON -> RetronFont
        AppFont.SYSTEM -> FontFamily.Default
    }

    return Typography().run {
        copy(
            displayLarge = displayLarge.withPixelFont(selectedFont, colors.TextColorLight),
            displayMedium = displayMedium.withPixelFont(selectedFont, colors.TextColorLight),
            displaySmall = displaySmall.withPixelFont(selectedFont, colors.TextColorLight),

            headlineLarge = headlineLarge.withPixelFont(selectedFont, colors.TextColorLight),
            headlineMedium = headlineMedium.withPixelFont(selectedFont, colors.TextColorLight),
            headlineSmall = headlineSmall.withPixelFont(selectedFont, colors.TextColorLight),

            titleLarge = titleLarge.withPixelFont(selectedFont, colors.Subtext1Color),
            titleMedium = titleMedium.withPixelFont(selectedFont, colors.Subtext1Color),
            titleSmall = titleSmall.withPixelFont(selectedFont, colors.Subtext1Color),

            bodyLarge = bodyLarge.withPixelFont(selectedFont, colors.TextColorLight),
            bodyMedium = bodyMedium.withPixelFont(selectedFont, colors.TextColorLight),
            bodySmall = bodySmall.withPixelFont(selectedFont, colors.TextColorLight),

            labelLarge = labelLarge.withPixelFont(selectedFont, colors.TextColorLight),
            labelMedium = labelMedium.withPixelFont(selectedFont, colors.TextColorLight),
            labelSmall = labelSmall.withPixelFont(selectedFont, colors.TextColorLight),
        )
    }
}

private fun TextStyle.withPixelFont(
    font: FontFamily,
    color: Color,
    scale: Float = 1f
): TextStyle {
    return copy(
        fontFamily = font,
        color = color,
        fontSize = fontSize * scale,
        lineHeight = lineHeight * scale,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}