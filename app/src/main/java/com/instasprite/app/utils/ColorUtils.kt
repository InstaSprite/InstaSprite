package com.instasprite.app.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt


fun Color.toContrastingTextColor(): Color {
    val dark = this.compositeOver(Color(0xFF808080)).luminance()
    val light = this.compositeOver(Color(0xFFC0C0C0)).luminance()
    val blackContrast = (minOf(dark, light) + 0.05f) / 0.05f
    val whiteContrast = 1.05f / (maxOf(dark, light) + 0.05f)
    return if (blackContrast >= whiteContrast) Color.Black else Color.White
}

fun convertHexToColor(hexColor: String): Color {
    return Color("#$hexColor".toColorInt())
}

fun Color.toHexString(includeAlpha: Boolean = false): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()

    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    } else {
        String.format("#%02X%02X%02X", red, green, blue)
    }
}