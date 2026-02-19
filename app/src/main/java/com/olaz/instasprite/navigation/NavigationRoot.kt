package com.olaz.instasprite.navigation

import androidx.navigation3.runtime.NavKey
import com.olaz.instasprite.domain.model.ColorPalette
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {

    @Serializable
    data object Gallery : Screen

    @Serializable
    data object Palette : Screen

    @Serializable
    data object CreateCanvas : Screen

    @Serializable
    data class Drawing(
        val spriteId: String,
        val width: Int,
        val height: Int,
        val spriteName: String?,
        val colorPalette: ColorPalette? = null
    ) : Screen
}