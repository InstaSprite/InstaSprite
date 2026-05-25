package com.instasprite.app.utils

import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class GplParseResult(
    val name: String,
    val colors: List<Color>
)

fun parseGplStream(inputStream: InputStream, fallbackName: String = "Unnamed"): GplParseResult? {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val colors = mutableListOf<Color>()
    var parsedName: String? = null
    var isGimpPalette = false

    try {
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            if (!isGimpPalette) {
                if (trimmed.startsWith("GIMP Palette", ignoreCase = true)) {
                    isGimpPalette = true
                    continue
                } else {
                    break
                }
            }

            if (trimmed.startsWith("Name:", ignoreCase = true)) {
                parsedName = trimmed.substringAfter(":").trim()
                continue
            }

            if (trimmed.startsWith("Columns:", ignoreCase = true)) {
                continue
            }

            if (trimmed.startsWith("#")) {
                continue
            }

            val parts = trimmed.split(Regex("\\s+"))
            if (parts.size >= 3) {
                val r = parts[0].toIntOrNull()
                val g = parts[1].toIntOrNull()
                val b = parts[2].toIntOrNull()
                if (r != null && g != null && b != null &&
                    r in 0..255 && g in 0..255 && b in 0..255
                ) {
                    colors.add(Color(red = r / 255f, green = g / 255f, blue = b / 255f))
                }
            }
        }
    } finally {
        reader.close()
    }

    return if (isGimpPalette && colors.isNotEmpty()) {
        GplParseResult(
            name = parsedName?.takeIf { it.isNotBlank() } ?: fallbackName,
            colors = colors
        )
    } else {
        null
    }
}
