package com.instasprite.app.navigation

import androidx.navigation3.runtime.NavKey
import com.instasprite.app.domain.model.ColorPalette
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {

    @Serializable
    data object Home : Screen

    @Serializable
    data object Gallery : Screen

    @Serializable
    data object Palette : Screen

    @Serializable
    data class PaletteEditor(val palette: ColorPalette? = null) : Screen

    @Serializable
    data object CreateCanvas : Screen

    @Serializable
    data object LoadImage : Screen

    @Serializable
    data class Drawing(
        val spriteId: String,
        val width: Int,
        val height: Int,
        val spriteName: String?,
        val colorPalette: ColorPalette? = null
    ) : Screen
    
    @Serializable
    data object Auth : Screen

    @Serializable
    data object Feed : Screen

    @Serializable
    data class Comments(val postId: Long) : Screen

    @Serializable
    data object CompletionProfile : Screen

    @Serializable
    data object CreatePost : Screen

    @Serializable
    data object Notification : Screen

    @Serializable
    data object Setting : Screen

    @Serializable
    data class Profile(val userId: String? = null) : Screen

    @Serializable
    data class Hashtag(val hashtag: String) : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object EditProfile : Screen
}