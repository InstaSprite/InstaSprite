package com.olaz.instasprite.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sprite_metadata",
    foreignKeys = [ForeignKey(
        entity = SpriteData::class,
        parentColumns = ["id"],
        childColumns = ["spriteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SpriteMetaData(
    @PrimaryKey val spriteId: String,
    val spriteName: String = "Untitled",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)