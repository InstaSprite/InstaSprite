package com.olaz.instasprite.navigation

import kotlinx.serialization.Serializable

sealed interface Route
@Serializable
object GalleryRoute : Route

@Serializable
data class DrawingRoute(
    val spriteId: String,
    val width: Int,
    val height: Int,
    val spriteName: String?
) : Route

@Serializable
object PaletteRoute : Route

@Serializable
object CreateCanvasRoute : Route
