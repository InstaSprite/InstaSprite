package com.instasprite.app.ui.gallery.contract

import android.content.Context
import com.instasprite.app.domain.model.Sprite

sealed interface SpriteListEvent {
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : SpriteListEvent
    data class OpenRenameDialog(val spriteId: String) : SpriteListEvent
    data class OpenPager(val sprite: Sprite) : SpriteListEvent
    data class OpenDrawingScreen(val name: String?, val sprite: Sprite, val context: Context) : SpriteListEvent
}