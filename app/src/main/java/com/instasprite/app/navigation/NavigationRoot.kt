package com.instasprite.app.navigation

import androidx.navigation3.runtime.NavKey
import com.instasprite.app.domain.model.ColorPalette
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {

    @Serializable
    data object Home : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Gallery : Screen

    @Serializable
    data class Palette(val clickToReturn: Boolean = false) : Screen

    @Serializable
    data class PaletteEditor(val palette: ColorPalette? = null) : Screen

    @Serializable
    data object CreateCanvas : Screen

    @Serializable
    data object LoadImage : Screen

    @Serializable
    data object Setting : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data class Drawing(
        val spriteId: String,
        val width: Int,
        val height: Int,
        val spriteName: String?,
        val colorPalette: ColorPalette? = null
    ) : Screen
}