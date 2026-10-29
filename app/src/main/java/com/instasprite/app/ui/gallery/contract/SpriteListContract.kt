package com.instasprite.app.ui.gallery.contract

import com.instasprite.app.domain.model.Sprite

sealed interface SpriteListEvent {
    data class TogglePlayback(val spriteId: String) : SpriteListEvent
    data class StopPlayback(val spriteId: String) : SpriteListEvent
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : SpriteListEvent
    data class OpenRenameDialog(val spriteId: String) : SpriteListEvent
    data class OpenPager(val sprite: Sprite) : SpriteListEvent
}
