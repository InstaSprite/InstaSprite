package com.instasprite.app.ui.gallery.contract

import android.content.Context
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteWithMeta

sealed interface ImagePagerEvent {
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : ImagePagerEvent
    data class OpenSaveImageDialog(val sprite: SpriteWithMeta) : ImagePagerEvent
    data class OpenDrawingActivity(val name: String?, val sprite: Sprite, val context: Context) : ImagePagerEvent
}


