package com.instasprite.app.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt


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