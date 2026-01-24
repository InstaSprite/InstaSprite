package com.olaz.instasprite.ui.gallery.contract

import android.content.Context
import com.olaz.instasprite.domain.model.Sprite

sealed interface SpriteListEvent {
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : SpriteListEvent
    data class OpenRenameDialog(val spriteId: String) : SpriteListEvent
    data class OpenPager(val sprite: Sprite) : SpriteListEvent
    data class OpenDrawingActivity(val sprite: Sprite, val context: Context) : SpriteListEvent
}