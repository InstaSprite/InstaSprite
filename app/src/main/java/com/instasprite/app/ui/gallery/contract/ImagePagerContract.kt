package com.instasprite.app.ui.gallery.contract

import com.instasprite.app.domain.model.SpriteWithMeta

sealed interface ImagePagerEvent {
    data class OpenDeleteDialog(val spriteName: String, val spriteId: String) : ImagePagerEvent
    data class OpenSaveImageDialog(val sprite: SpriteWithMeta) : ImagePagerEvent
}


