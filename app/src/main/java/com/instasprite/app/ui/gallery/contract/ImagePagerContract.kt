package com.instasprite.app.ui.gallery.contract

import com.instasprite.app.domain.model.SpriteWithMeta

sealed interface ImagePagerEvent {
    data class TogglePlayback(val spriteId: String) : ImagePagerEvent
    data class StopPlayback(val spriteId: String) : ImagePagerEvent
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : ImagePagerEvent
    data class Export(val sprite: SpriteWithMeta, val format: GalleryExportFormat) : ImagePagerEvent
}



enum class GalleryExportFormat { IMAGE, GIF, SPRITE_SHEET, PROJECT }
