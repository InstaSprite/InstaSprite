package com.instasprite.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SpriteWithMetaData(
    @Embedded val sprite: SpriteData,

    @Relation(
        parentColumn = "id",
        entityColumn = "spriteId"
    )
    val meta: SpriteMetaData?
)