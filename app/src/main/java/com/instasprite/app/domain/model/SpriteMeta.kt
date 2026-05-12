package com.instasprite.app.domain.model

data class SpriteMeta(
    val spriteId: String,
    val spriteName: String = "Untitled",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)
