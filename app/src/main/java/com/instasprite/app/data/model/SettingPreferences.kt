package com.instasprite.app.data.model

import com.instasprite.app.ui.gallery.GalleryLayoutMode
import com.instasprite.app.ui.gallery.SpriteListOrder
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.ThemeFlavour
import kotlinx.serialization.Serializable

@Serializable
data class SettingPreferences(
    val language: String = "en",
    val themeFlavour: ThemeFlavour = ThemeFlavour.MOCHA,
    val appFont: AppFont = AppFont.DETERMINATION,
    val drawSetting: DrawSetting = DrawSetting(),
    val gallerySettings: GallerySettings = GallerySettings(),
    val prepopulatedPaletteVersion: Int = 0
)

@Serializable
data class DrawSetting(
    val isCursorMode: Boolean = false
)

@Serializable
data class GallerySettings(
    val sortOrder: SpriteListOrder = SpriteListOrder.DateCreatedDesc,
    val layoutMode: GalleryLayoutMode = GalleryLayoutMode.List
)