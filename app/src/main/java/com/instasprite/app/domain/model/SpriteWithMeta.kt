package com.instasprite.app.domain.model

data class SpriteWithMeta(
    val sprite: Sprite,
    val meta: SpriteMeta?,
    val hasAnimation: Boolean = (sprite.animation?.frames?.size ?: 0) > 1
)
