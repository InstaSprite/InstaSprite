package com.olaz.instasprite.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.olaz.instasprite.data.database.ColorPaletteConverters

@Entity(tableName = "palette_data")
@TypeConverters(ColorPaletteConverters::class)
data class ColorPaletteData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "Unnamed",
    val author: String = "Anonymous",
    var colors: MutableList<Color> = mutableListOf(),
)