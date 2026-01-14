package com.olaz.instasprite.domain.model

import androidx.compose.ui.graphics.Color

data class ColorPaletteModel(
    val id: Int = 0,
    val name: String = "Unnamed",
    val author: String = "Anonymous",
    var colors: MutableList<Color> = mutableListOf(),
)