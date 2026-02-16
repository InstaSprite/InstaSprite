package com.olaz.instasprite.utils

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.domain.model.ColorPalette


object DummyData {

    val palettes = listOf(
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
}