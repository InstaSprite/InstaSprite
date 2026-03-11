package com.olaz.instasprite.data.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter

class ColorPaletteConverters {

    @TypeConverter
    fun fromColorList(colors: List<Color>?): String {
        if (colors == null) return ""
        return colors.joinToString(",") { it.toArgb().toString() }
    }

    @TypeConverter
    fun toColorList(data: String?): MutableList<Color> {
        if (data.isNullOrEmpty()) return mutableListOf()
        return data.split(",")
            .mapNotNull { it.toIntOrNull() }
            .map { Color(it) }
            .toMutableList()
    }
}