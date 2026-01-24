package com.olaz.instasprite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

@Entity(tableName = "sprite_data")
@TypeConverters(IntListConverter::class)
@Serializable
data class SpriteData(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    val width: Int,
    val height: Int,
    val pixelsData: List<Int>,
    val colorPalette: List<Int>? = null
)
