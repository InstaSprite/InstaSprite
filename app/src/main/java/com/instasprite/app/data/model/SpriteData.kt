package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.instasprite.app.data.database.AppTypeConverters
import kotlinx.serialization.Serializable

@Entity(tableName = "sprite_data")
@TypeConverters(AppTypeConverters::class)
@Serializable
data class SpriteData(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    val width: Int,
    val height: Int
)
