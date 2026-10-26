package com.instasprite.app.domain.model

data class DrawingCheckpoint(
    val sprite: Sprite,
    val activeFrameId: String = "",
    val activeLayerId: String = "",
    val unfinishedSession: Boolean = false
)
